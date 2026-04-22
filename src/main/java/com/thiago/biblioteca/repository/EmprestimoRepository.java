package com.thiago.biblioteca.repository;

import com.thiago.biblioteca.model.Emprestimo;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EmprestimoRepository extends MongoRepository<Emprestimo, String> {
    List<Emprestimo> findByUsuarioId(String usuarioId);
    List<Emprestimo> findByStatus(String status);
    List<Emprestimo> findByLivroId(String livroId);
    List<Emprestimo> findByUsuarioIdAndStatus(String usuarioId, String status);
    long countByUsuarioIdAndStatus(String usuarioId, String status);

    // Verificar se usuário já tem um título específico em determinado status
    List<Emprestimo> findByUsuarioIdAndLivroIdAndStatus(String usuarioId, String livroId, String status);
}