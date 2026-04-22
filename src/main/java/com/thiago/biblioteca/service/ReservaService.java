package com.thiago.biblioteca.service;

import com.thiago.biblioteca.model.Livro;
import com.thiago.biblioteca.model.Reserva;
import com.thiago.biblioteca.model.Usuario;
import com.thiago.biblioteca.repository.EmprestimoRepository;
import com.thiago.biblioteca.repository.LivroRepository;
import com.thiago.biblioteca.repository.ReservaRepository;
import com.thiago.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final EmprestimoRepository emprestimoRepository;

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    public List<Reserva> listarPorUsuario(String usuarioId) {
        return reservaRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Reserva> buscarPorId(String id) {
        return reservaRepository.findById(id);
    }

    public Reserva realizarReserva(Reserva reserva) {
        String usuarioId = reserva.getUsuarioId();
        String livroId   = reserva.getLivroId();

        // Regra 1: livro deve estar indisponível (reserva só faz sentido quando não há exemplares)
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado."));
        if (livro.isDisponivel()) {
            throw new RuntimeException(
                "Este livro está disponível. Solicite o empréstimo diretamente — não é necessário reservar.");
        }

        // Regra 2: usuário com empréstimo ATRASADO está bloqueado
        long atrasados = emprestimoRepository.countByUsuarioIdAndStatus(usuarioId, "ATRASADO");
        if (atrasados > 0) {
            throw new RuntimeException(
                "Você possui empréstimo(s) em atraso. Regularize sua situação antes de realizar reservas.");
        }

        // Regra 3: usuário não pode reservar livro que já tem emprestado (ATIVO ou ATRASADO)
        boolean jaEmprestadoAtivo    = !emprestimoRepository
                .findByUsuarioIdAndLivroIdAndStatus(usuarioId, livroId, "ATIVO").isEmpty();
        boolean jaEmprestadoAtrasado = !emprestimoRepository
                .findByUsuarioIdAndLivroIdAndStatus(usuarioId, livroId, "ATRASADO").isEmpty();
        if (jaEmprestadoAtivo || jaEmprestadoAtrasado) {
            throw new RuntimeException("Você já possui este livro emprestado.");
        }

        // Regra 4: máximo 1 reserva AGUARDANDO do mesmo título por usuário
        List<Reserva> reservaExistente = reservaRepository
                .findByUsuarioIdAndLivroIdAndStatus(usuarioId, livroId, "AGUARDANDO");
        if (!reservaExistente.isEmpty()) {
            throw new RuntimeException(
                "Você já possui uma reserva ativa para este livro. Aguarde a notificação de disponibilidade.");
        }

        // Buscar usuário
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // Criar reserva (entra no fim da fila FIFO — dataReserva define a ordem)
        reserva.setNomeUsuario(usuario.getNome());
        reserva.setTituloLivro(livro.getTitulo());
        reserva.setDataReserva(LocalDate.now());
        reserva.setStatus("AGUARDANDO");
        reserva.setDisponivelParaRetirada(false);

        return reservaRepository.save(reserva);
    }

    public Reserva cancelar(String id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        reserva.setStatus("CANCELADA");
        reserva.setDisponivelParaRetirada(false);
        return reservaRepository.save(reserva);
    }

    public Reserva concluir(String id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        reserva.setStatus("CONCLUIDA");
        reserva.setDisponivelParaRetirada(false);
        return reservaRepository.save(reserva);
    }

    public void deletar(String id) {
        reservaRepository.deleteById(id);
    }

    /**
     * Job que roda a cada hora e expira reservas com mais de 48h sem confirmação.
     * Ao cancelar, passa automaticamente para o próximo da fila FIFO.
     */
    @Scheduled(fixedRate = 3_600_000) // 1 hora em milissegundos
    public void expirarReservasAntigas() {
        LocalDateTime limite = LocalDateTime.now().minusHours(48);

        List<Reserva> pendentes = reservaRepository.findByDisponivelParaRetiradaTrue();
        for (Reserva reserva : pendentes) {
            if (reserva.getDataNotificacao() == null
                    || !reserva.getDataNotificacao().isBefore(limite)) {
                continue; // ainda dentro das 48h
            }

            log.info("Reserva {} expirada (48h sem confirmação). Cancelando e passando para próximo da fila.",
                    reserva.getId());

            // Cancelar reserva expirada
            reserva.setStatus("CANCELADA");
            reserva.setDisponivelParaRetirada(false);
            reservaRepository.save(reserva);

            // Notificar próximo da fila FIFO para este livro
            List<Reserva> proximos = reservaRepository
                    .findByLivroIdAndStatusOrderByDataReservaAsc(reserva.getLivroId(), "AGUARDANDO");
            if (!proximos.isEmpty()) {
                Reserva proximo = proximos.get(0);
                proximo.setDisponivelParaRetirada(true);
                proximo.setDataNotificacao(LocalDateTime.now());
                reservaRepository.save(proximo);
                log.info("Reserva {} (usuário {}) notificada — livro {}.",
                        proximo.getId(), proximo.getNomeUsuario(), proximo.getTituloLivro());
            }
        }
    }
}
