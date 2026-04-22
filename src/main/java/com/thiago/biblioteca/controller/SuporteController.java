package com.thiago.biblioteca.controller;

import com.thiago.biblioteca.model.Suporte;
import com.thiago.biblioteca.service.SuporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suporte")
@RequiredArgsConstructor
public class SuporteController {

    private final SuporteService suporteService;

    // ─────────────────────────────────────────────
    //  ROTAS PÚBLICAS
    // ─────────────────────────────────────────────

    /** Formulário público de abertura de chamado */
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("suporte", new Suporte());
        return "suporte/novo";
    }

    /** Salva o chamado e mostra mensagem com o e-mail do usuário */
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Suporte suporte, Model model) {
        String emailUsuario = suporte.getEmail();
        suporteService.salvar(suporte);
        model.addAttribute("sucesso", true);
        model.addAttribute("emailEnviado", emailUsuario);
        model.addAttribute("suporte", new Suporte());
        return "suporte/novo";
    }

    /**
     * Acompanhamento de chamados — exige autenticação (qualquer role).
     * Busca automaticamente pelo e-mail do usuário logado.
     */
    @GetMapping("/acompanhar")
    public String acompanhar(Authentication auth, Model model) {
        String email = auth.getName(); // retorna o e-mail (username)
        model.addAttribute("chamados", suporteService.listarPorEmail(email));
        return "suporte/acompanhar";
    }

    /** Salva avaliação de estrelas + reação para um chamado respondido */
    @PostMapping("/avaliar/{id}")
    public String avaliar(@PathVariable String id,
                          @RequestParam Integer estrelas,
                          @RequestParam String reacao,
                          Authentication auth,
                          RedirectAttributes ra) {
        String email = auth.getName();
        suporteService.buscarPorId(id).ifPresentOrElse(c -> {
            if (!email.equalsIgnoreCase(c.getEmail())) {
                ra.addFlashAttribute("erro", "Você não pode avaliar este chamado.");
                return;
            }
            if (c.getAvaliacaoEstrelas() != null) {
                ra.addFlashAttribute("erro", "Este chamado já foi avaliado.");
                return;
            }
            suporteService.avaliar(id, estrelas, reacao);
            ra.addFlashAttribute("sucesso", "Avaliação registrada! Obrigado pelo feedback.");
        }, () -> ra.addFlashAttribute("erro", "Chamado não encontrado."));
        return "redirect:/suporte/acompanhar";
    }

    // ─────────────────────────────────────────────
    //  ROTAS DO BIBLIOTECÁRIO (admin)
    // ─────────────────────────────────────────────

    /** Listagem de todos os chamados */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("chamados", suporteService.listarTodos());
        return "suporte/lista";
    }

    /** Formulário de resposta ao chamado */
    @GetMapping("/responder/{id}")
    public String responderForm(@PathVariable String id, Model model) {
        suporteService.buscarPorId(id).ifPresent(c -> model.addAttribute("chamado", c));
        return "suporte/responder";
    }

    /** Envia a resposta, marca como RESOLVIDO */
    @PostMapping("/responder/{id}")
    public String responderEnviar(@PathVariable String id,
                                  @RequestParam String resposta,
                                  RedirectAttributes ra) {
        suporteService.responder(id, resposta);
        ra.addFlashAttribute("sucesso", "Resposta enviada com sucesso!");
        return "redirect:/suporte";
    }

    /** Visualiza chamado completo com resposta */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable String id, Model model) {
        suporteService.buscarPorId(id).ifPresent(c -> model.addAttribute("chamado", c));
        return "suporte/ver";
    }

    /** Marca chamado como resolvido sem resposta */
    @PostMapping("/resolver/{id}")
    public String resolver(@PathVariable String id, RedirectAttributes ra) {
        suporteService.resolver(id);
        ra.addFlashAttribute("sucesso", "Chamado marcado como resolvido.");
        return "redirect:/suporte";
    }

    /** Deleta o chamado */
    @PostMapping("/deletar/{id}")
    public String deletar(@PathVariable String id, RedirectAttributes ra) {
        suporteService.deletar(id);
        ra.addFlashAttribute("sucesso", "Chamado excluído.");
        return "redirect:/suporte";
    }
}
