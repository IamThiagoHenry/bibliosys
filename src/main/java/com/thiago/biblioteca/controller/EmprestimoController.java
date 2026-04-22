package com.thiago.biblioteca.controller;

import com.thiago.biblioteca.model.Emprestimo;
import com.thiago.biblioteca.service.EmprestimoService;
import com.thiago.biblioteca.service.LivroService;
import com.thiago.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/emprestimos")
@RequiredArgsConstructor
public class EmprestimoController {

    private final EmprestimoService emprestimoService;
    private final LivroService livroService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("emprestimos", emprestimoService.listarTodos());
        return "emprestimos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("emprestimo", new Emprestimo());
        model.addAttribute("livros", livroService.listarTodos());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "emprestimos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Emprestimo emprestimo, Model model) {
        try {
            emprestimoService.realizarEmprestimo(emprestimo);
            return "redirect:/emprestimos";
        } catch (RuntimeException e) {
            // Re-renderiza o formulário com a mensagem de erro mantendo os dados
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("livros", livroService.listarTodos());
            model.addAttribute("usuarios", usuarioService.listarTodos());
            return "emprestimos/form";
        }
    }

    @PostMapping("/devolver/{id}")
    public String devolver(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            emprestimoService.realizarDevolucao(id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/emprestimos";
    }
}
