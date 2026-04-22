package com.thiago.biblioteca.controller;

import com.thiago.biblioteca.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping
    public String index(Model model) {
        // ── Cards de resumo ─────────────────────────────────────────────────────
        model.addAttribute("totalLivros",         relatorioService.totalLivros());
        model.addAttribute("totalUsuarios",       relatorioService.totalUsuarios());
        model.addAttribute("totalAtivos",         relatorioService.totalEmprestimosAtivos()); // ATIVO+ATRASADO
        model.addAttribute("totalDevolvidos",     relatorioService.totalDevolvidos());
        model.addAttribute("chamadosAbertos",     relatorioService.totalChamadosAbertos());

        // ── Gráfico de pizza (status separados) ─────────────────────────────────
        model.addAttribute("chartAtivos",         relatorioService.totalEmprestimosApenasAtivos());
        model.addAttribute("chartDevolvidos",     relatorioService.totalDevolvidos());
        model.addAttribute("chartAtrasados",      relatorioService.totalEmprestimosAtrasados());

        // ── Tabelas e gráfico de barras ──────────────────────────────────────────
        model.addAttribute("top5Livros",          relatorioService.top5LivrosMaisEmprestados());
        model.addAttribute("ultimos5Emprestimos", relatorioService.ultimos5Emprestimos());
        model.addAttribute("top5Usuarios",        relatorioService.top5UsuariosMaisAtivos());

        return "relatorios/index";
    }
}
