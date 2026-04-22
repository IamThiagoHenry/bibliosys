package com.thiago.biblioteca.repository;

import com.thiago.biblioteca.model.Reserva;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReservaRepository extends MongoRepository<Reserva, String> {
    List<Reserva> findByUsuarioId(String usuarioId);
    List<Reserva> findByLivroId(String livroId);
    List<Reserva> findByStatus(String status);
    List<Reserva> findByLivroIdAndStatus(String livroId, String status);

    // Fila FIFO: reservas aguardando de um livro ordenadas pela data de criação
    List<Reserva> findByLivroIdAndStatusOrderByDataReservaAsc(String livroId, String status);

    // Verificar se usuário já tem reserva ativa para um livro específico
    List<Reserva> findByUsuarioIdAndLivroIdAndStatus(String usuarioId, String livroId, String status);

    // Reservas marcadas disponíveis (para controle das 48h)
    List<Reserva> findByDisponivelParaRetiradaTrue();
}
