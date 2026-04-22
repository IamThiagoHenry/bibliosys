package com.thiago.biblioteca.controller;

import com.thiago.biblioteca.model.Livro;
import com.thiago.biblioteca.service.LivroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/livros")
@RequiredArgsConstructor
public class LivroController {

    private final LivroService livroService;

    @GetMapping
    public String listar(Model model, @RequestParam(required = false) String busca) {
        if (busca != null && !busca.isEmpty()) {
            model.addAttribute("livros", livroService.buscarPorTitulo(busca));
        } else {
            model.addAttribute("livros", livroService.listarTodos());
        }
        model.addAttribute("busca", busca);
        return "livros/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("livro", new Livro());
        return "livros/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Livro livro) {
        livroService.salvar(livro);
        return "redirect:/livros";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable String id, Model model) {
        livroService.buscarPorId(id).ifPresent(l -> model.addAttribute("livro", l));
        return "livros/form";
    }

    @PostMapping("/deletar/{id}")
    public String deletar(@PathVariable String id) {
        if (id == null || id.isBlank()) {
            return "redirect:/livros";
        }
        livroService.deletar(id);
        return "redirect:/livros";
    }
}