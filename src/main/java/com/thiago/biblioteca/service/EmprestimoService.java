package com.thiago.biblioteca.service;

import com.thiago.biblioteca.model.Emprestimo;
import com.thiago.biblioteca.model.Livro;
import com.thiago.biblioteca.model.Reserva;
import com.thiago.biblioteca.model.Usuario;
import com.thiago.biblioteca.repository.EmprestimoRepository;
import com.thiago.biblioteca.repository.LivroRepository;
import com.thiago.biblioteca.repository.ReservaRepository;
import com.thiago.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public List<Emprestimo> listarTodos() {
        return atualizarStatusEMulta(emprestimoRepository.findAll());
    }

    public List<Emprestimo> listarPorUsuario(String usuarioId) {
        return atualizarStatusEMulta(emprestimoRepository.findByUsuarioId(usuarioId));
    }

    /**
     * Percorre a lista e:
     *  - Marca como ATRASADO os empréstimos ATIVO com dataDevolucao < hoje
     *  - Calcula a multa de R$1,00 por dia de atraso
     */
    private List<Emprestimo> atualizarStatusEMulta(List<Emprestimo> lista) {
        LocalDate hoje = LocalDate.now();
        List<Emprestimo> resultado = new ArrayList<>();

        for (Emprestimo e : lista) {
            if ("ATIVO".equals(e.getStatus()) && e.getDataDevolucao() != null) {
                long diasRestantes = ChronoUnit.DAYS.between(hoje, e.getDataDevolucao());

                if (diasRestantes < 0) {
                    // Passou do prazo → ATRASADO
                    e.setStatus("ATRASADO");
                    e.setMulta(Math.abs(diasRestantes) * 1.0);
                    emprestimoRepository.save(e);
                } else if (diasRestantes <= 3) {
                    // Vence em até 3 dias → alerta visual (não persiste)
                    e.setVencendoEmBreve(true);
                }

            } else if ("ATRASADO".equals(e.getStatus()) && e.getDataDevolucao() != null) {
                long dias = ChronoUnit.DAYS.between(e.getDataDevolucao(), hoje);
                e.setMulta(Math.max(0, dias) * 1.0);
            }
            resultado.add(e);
        }
        return resultado;
    }

    public Emprestimo realizarEmprestimo(Emprestimo emprestimo) {
        String usuarioId = emprestimo.getUsuarioId();
        String livroId   = emprestimo.getLivroId();

        // Regra 1: usuário com empréstimo ATRASADO está bloqueado
        long atrasados = emprestimoRepository.countByUsuarioIdAndStatus(usuarioId, "ATRASADO");
        if (atrasados > 0) {
            throw new RuntimeException(
                "Você possui empréstimo(s) em atraso. Regularize sua situação antes de realizar novos empréstimos.");
        }

        // Regra 2: máximo 1 exemplar do mesmo título por usuário
        boolean jaTemTitulo = !emprestimoRepository
                .findByUsuarioIdAndLivroIdAndStatus(usuarioId, livroId, "ATIVO").isEmpty();
        if (jaTemTitulo) {
            throw new RuntimeException("Você já possui este título emprestado.");
        }

        // Regra 3: máximo 3 títulos diferentes ativos
        long ativos = emprestimoRepository.countByUsuarioIdAndStatus(usuarioId, "ATIVO");
        if (ativos >= 3) {
            throw new RuntimeException(
                "Limite de 3 empréstimos simultâneos atingido. Devolva um livro antes de realizar novo empréstimo.");
        }

        // Regra 4: livro deve estar disponível
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado."));
        if (!livro.isDisponivel()) {
            throw new RuntimeException(
                "Livro indisponível. Se desejar, você pode reservá-lo para ser atendido quando houver devolução.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // Regra 5: se o usuário tem reserva AGUARDANDO deste livro → cancelar automaticamente
        List<Reserva> reservasDoUsuario = reservaRepository
                .findByUsuarioIdAndLivroIdAndStatus(usuarioId, livroId, "AGUARDANDO");
        for (Reserva r : reservasDoUsuario) {
            r.setStatus("CANCELADA");
            r.setDisponivelParaRetirada(false);
            reservaRepository.save(r);
        }

        // Decrementar estoque
        livro.setQuantidade(livro.getQuantidade() - 1);
        livro.setDisponivel(livro.getQuantidade() > 0);
        livroRepository.save(livro);

        // Preencher e salvar empréstimo
        emprestimo.setTituloLivro(livro.getTitulo());
        emprestimo.setNomeUsuario(usuario.getNome());
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimo.setDataDevolucao(LocalDate.now().plusDays(14));
        emprestimo.setStatus("ATIVO");
        emprestimo.setMulta(0.0);

        return emprestimoRepository.save(emprestimo);
    }

    public Emprestimo realizarDevolucao(String emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado"));

        Livro livro = livroRepository.findById(emprestimo.getLivroId())
                .orElseThrow(() -> new RuntimeException("Livro não encontrado"));

        // Devolve ao estoque
        livro.setQuantidade(livro.getQuantidade() + 1);
        livro.setDisponivel(true);
        livroRepository.save(livro);

        emprestimo.setStatus("DEVOLVIDO");
        emprestimo.setMulta(0.0);
        emprestimo.setDataRetornoReal(LocalDate.now()); // registra data real de entrega
        Emprestimo salvo = emprestimoRepository.save(emprestimo);

        // Notifica a primeira reserva da fila FIFO para este livro
        List<Reserva> fila = reservaRepository
                .findByLivroIdAndStatusOrderByDataReservaAsc(livro.getId(), "AGUARDANDO");
        if (!fila.isEmpty()) {
            Reserva primeira = fila.get(0);
            primeira.setDisponivelParaRetirada(true);
            primeira.setDataNotificacao(LocalDateTime.now()); // marco para o contador das 48h
            reservaRepository.save(primeira);
        }

        return salvo;
    }
}
