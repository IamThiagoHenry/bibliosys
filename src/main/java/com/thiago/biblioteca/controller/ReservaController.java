package com.thiago.biblioteca.controller;

import com.thiago.biblioteca.model.Reserva;
import com.thiago.biblioteca.service.LivroService;
import com.thiago.biblioteca.service.ReservaService;
import com.thiago.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;
    private final LivroService livroService;
    private final UsuarioService usuarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("reservas", reservaService.listarTodas());
        return "reservas/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("livros", livroService.listarTodos());
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "reservas/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Reserva reserva, Model model) {
        try {
            reservaService.realizarReserva(reserva);
            return "redirect:/reservas";
        } catch (RuntimeException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("livros", livroService.listarTodos());
            model.addAttribute("usuarios", usuarioService.listarTodos());
            return "reservas/form";
        }
    }

    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            reservaService.cancelar(id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/reservas";
    }

    @PostMapping("/concluir/{id}")
    public String concluir(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            reservaService.concluir(id);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/reservas";
    }

    @PostMapping("/deletar/{id}")
    public String deletar(@PathVariable String id) {
        reservaService.deletar(id);
        return "redirect:/reservas";
    }
}
