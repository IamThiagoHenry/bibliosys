package com.thiago.biblioteca.service;

import com.thiago.biblioteca.model.Livro;
import com.thiago.biblioteca.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LivroService {

    private final LivroRepository livroRepository;

    public List<Livro> listarTodos() {
        return livroRepository.findAll();
    }

    public Optional<Livro> buscarPorId(String id) {
        return livroRepository.findById(id);
    }

    public List<Livro> buscarPorTitulo(String titulo) {
        return livroRepository.findByTituloContainingIgnoreCase(titulo);
    }

    public Livro salvar(Livro livro) {
        if (livro.getId() != null && livro.getId().isBlank()) {
            livro.setId(null);
        }
        livro.setDisponivel(livro.getQuantidade() > 0);
        return livroRepository.save(livro);
    }

    public void deletar(String id) {
        livroRepository.deleteById(id);
    }
}