package com.thiago.biblioteca.service;

import com.thiago.biblioteca.model.Emprestimo;
import com.thiago.biblioteca.model.Livro;
import com.thiago.biblioteca.repository.EmprestimoRepository;
import com.thiago.biblioteca.repository.LivroRepository;
import com.thiago.biblioteca.repository.SuporteRepository;
import com.thiago.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final SuporteRepository suporteRepository;

    public long totalLivros() {
        return livroRepository.count();
    }

    public long totalUsuarios() {
        return usuarioRepository.count();
    }

    /** ATIVO + ATRASADO — usado no card de resumo */
    public long totalEmprestimosAtivos() {
        return emprestimoRepository.findByStatus("ATIVO").size()
             + emprestimoRepository.findByStatus("ATRASADO").size();
    }

    /** Apenas ATIVO — usado no gráfico de pizza */
    public long totalEmprestimosApenasAtivos() {
        return emprestimoRepository.findByStatus("ATIVO").size();
    }

    public long totalEmprestimosAtrasados() {
        return emprestimoRepository.findByStatus("ATRASADO").size();
    }

    public long totalDevolvidos() {
        return emprestimoRepository.findByStatus("DEVOLVIDO").size();
    }

    public long totalChamadosAbertos() {
        return suporteRepository.findByStatus("ABERTO").size();
    }

    /** Usuários distintos com pelo menos um empréstimo ATIVO ou ATRASADO */
    public long totalUsuariosAtivos() {
        Set<String> ids = Stream.concat(
                emprestimoRepository.findByStatus("ATIVO").stream(),
                emprestimoRepository.findByStatus("ATRASADO").stream()
        ).map(Emprestimo::getUsuarioId).collect(Collectors.toSet());
        return ids.size();
    }

    /** Últimos 5 empréstimos, mais recentes primeiro */
    public List<Emprestimo> ultimos5Emprestimos() {
        return emprestimoRepository.findAll().stream()
                .sorted(Comparator.comparing(Emprestimo::getDataEmprestimo,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList());
    }

    /** Top 5 livros com campos: titulo, totalEmprestimos */
    public List<Map<String, Object>> top5LivrosMaisEmprestados() {
        List<Emprestimo> todos = emprestimoRepository.findAll();

        Map<String, Long> contagem = todos.stream()
                .collect(Collectors.groupingBy(Emprestimo::getLivroId, Collectors.counting()));

        return contagem.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    String titulo = livroRepository.findById(entry.getKey())
                            .map(Livro::getTitulo)
                            .orElse("(livro removido)");
                    item.put("titulo", titulo);
                    item.put("totalEmprestimos", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /** Top 5 usuários com campos: nome, email, totalEmprestimos */
    public List<Map<String, Object>> top5UsuariosMaisAtivos() {
        List<Emprestimo> todos = emprestimoRepository.findAll();

        Map<String, Long> contagem = todos.stream()
                .collect(Collectors.groupingBy(Emprestimo::getUsuarioId, Collectors.counting()));

        return contagem.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    usuarioRepository.findById(entry.getKey()).ifPresentOrElse(u -> {
                        item.put("nome", u.getNome());
                        item.put("email", u.getEmail());
                    }, () -> {
                        item.put("nome", "(usuário removido)");
                        item.put("email", "—");
                    });
                    item.put("totalEmprestimos", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
