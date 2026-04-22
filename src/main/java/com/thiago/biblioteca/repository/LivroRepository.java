package com.thiago.biblioteca.repository;

import com.thiago.biblioteca.model.Livro;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface LivroRepository extends MongoRepository<Livro, String> {
    List<Livro> findByTituloContainingIgnoreCase(String titulo);
    List<Livro> findByAutorContainingIgnoreCase(String autor);
    List<Livro> findByDisponivel(boolean disponivel);
}