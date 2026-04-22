package com.thiago.biblioteca.controller;

import com.thiago.biblioteca.repository.EmprestimoRepository;
import com.thiago.biblioteca.repository.LivroRepository;
import com.thiago.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalLivros",   livroRepository.count());
        model.addAttribute("totalUsuarios", usuarioRepository.count());
        long ativos = emprestimoRepository.findByStatus("ATIVO").size()
                    + emprestimoRepository.findByStatus("ATRASADO").size();
        model.addAttribute("totalAtivos", ativos);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
