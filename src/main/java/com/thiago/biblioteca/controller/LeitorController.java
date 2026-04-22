package com.thiago.biblioteca.controller;

import com.thiago.biblioteca.model.Emprestimo;
import com.thiago.biblioteca.model.Reserva;
import com.thiago.biblioteca.model.UsuarioDetails;
import com.thiago.biblioteca.service.EmprestimoService;
import com.thiago.biblioteca.service.LivroService;
import com.thiago.biblioteca.service.ReservaService;
import com.thiago.biblioteca.service.SuporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class LeitorController {

    private final EmprestimoService emprestimoService;
    private final ReservaService reservaService;
    private final LivroService livroService;
    private final SuporteService suporteService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/leitor/dashboard")
    public String dashboard(Authentication auth, Model model) {
        UsuarioDetails details = (UsuarioDetails) auth.getPrincipal();
        String email = details.getUsername();

        // Todos os empréstimos do usuário (com status/multa atualizados)
        List<Emprestimo> meusEmprestimos = emprestimoService.listarPorUsuario(details.getId());

        long totalAtivos    = meusEmprestimos.stream().filter(e -> "ATIVO".equals(e.getStatus())).count();
        long totalAtrasados = meusEmprestimos.stream().filter(e -> "ATRASADO".equals(e.getStatus())).count();
        long totalDevolvidos= meusEmprestimos.stream().filter(e -> "DEVOLVIDO".equals(e.getStatus())).count();

        List<Reserva> minhasReservas = reservaService.listarPorUsuario(details.getId());
        long reservasAguardando = minhasReservas.stream()
                .filter(r -> "AGUARDANDO".equals(r.getStatus())).count();
        List<Reserva> reservasAtivas = minhasReservas.stream()
                .filter(r -> "AGUARDANDO".equals(r.getStatus()))
                .collect(Collectors.toList());

        long livrosDisponiveis = livroService.listarTodos().stream()
                .filter(l -> l.isDisponivel()).count();

        long chamadosAbertos = suporteService.listarPorEmail(email).stream()
                .filter(c -> "ABERTO".equals(c.getStatus())).count();

        List<Emprestimo> ultimosEmprestimos = meusEmprestimos.stream()
                .sorted(Comparator.comparing(Emprestimo::getDataEmprestimo,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .collect(Collectors.toList());

        // ── Cards de resumo ─────────────────────────────────────────────────────
        model.addAttribute("nomeUsuario",              details.getNome());
        model.addAttribute("meusEmprestimosAtivos",    totalAtivos + totalAtrasados);
        model.addAttribute("minhasReservasAguardando", reservasAguardando);
        model.addAttribute("livrosDisponiveis",        livrosDisponiveis);
        model.addAttribute("chamadosAbertos",          chamadosAbertos);

        // ── Dados para o gráfico doughnut ───────────────────────────────────────
        model.addAttribute("totalAtivos",     totalAtivos);
        model.addAttribute("totalDevolvidos", totalDevolvidos);
        model.addAttribute("totalAtrasados",  totalAtrasados);

        // ── Listas ──────────────────────────────────────────────────────────────
        model.addAttribute("ultimosEmprestimos", ultimosEmprestimos);
        model.addAttribute("minhasReservas",     reservasAtivas);

        return "leitor/dashboard";
    }

    // ── Meus Empréstimos ──────────────────────────────────────────────────────

    @GetMapping("/meus-emprestimos")
    public String meusEmprestimos(Authentication auth, Model model) {
        UsuarioDetails details = (UsuarioDetails) auth.getPrincipal();
        model.addAttribute("emprestimos", emprestimoService.listarPorUsuario(details.getId()));
        return "leitor/meus-emprestimos";
    }

    // ── Minhas Reservas ───────────────────────────────────────────────────────

    @GetMapping("/minhas-reservas")
    public String minhasReservas(Authentication auth, Model model) {
        UsuarioDetails details = (UsuarioDetails) auth.getPrincipal();
        model.addAttribute("reservas", reservaService.listarPorUsuario(details.getId()));
        return "leitor/minhas-reservas";
    }

    /** Leitor cancela sua própria reserva — verifica ownership antes de cancelar */
    @PostMapping("/minhas-reservas/cancelar/{id}")
    public String cancelarReserva(@PathVariable String id,
                                  Authentication auth,
                                  RedirectAttributes ra) {
        UsuarioDetails details = (UsuarioDetails) auth.getPrincipal();
        reservaService.buscarPorId(id).ifPresentOrElse(reserva -> {
            if (!details.getId().equals(reserva.getUsuarioId())) {
                ra.addFlashAttribute("erro", "Você não tem permissão para cancelar esta reserva.");
                return;
            }
            if (!"AGUARDANDO".equals(reserva.getStatus())) {
                ra.addFlashAttribute("erro", "Apenas reservas com status AGUARDANDO podem ser canceladas.");
                return;
            }
            reservaService.cancelar(id);
            ra.addFlashAttribute("sucesso", "Reserva cancelada com sucesso.");
        }, () -> ra.addFlashAttribute("erro", "Reserva não encontrada."));
        return "redirect:/minhas-reservas";
    }

    // ── Catálogo ──────────────────────────────────────────────────────────────

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String busca, Model model) {
        if (busca != null && !busca.isBlank()) {
            model.addAttribute("livros", livroService.buscarPorTitulo(busca));
        } else {
            model.addAttribute("livros", livroService.listarTodos());
        }
        model.addAttribute("busca", busca);
        return "leitor/catalogo";
    }

    /** Leitor solicita empréstimo diretamente pelo catálogo */
    @PostMapping("/catalogo/solicitar")
    public String solicitar(@RequestParam String livroId,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        UsuarioDetails details = (UsuarioDetails) auth.getPrincipal();
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setLivroId(livroId);
        emprestimo.setUsuarioId(details.getId());
        try {
            emprestimoService.realizarEmprestimo(emprestimo);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Empréstimo realizado com sucesso! Prazo de devolução: 14 dias.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/catalogo";
    }

    /** Leitor reserva livro indisponível pelo catálogo */
    @PostMapping("/catalogo/reservar")
    public String reservar(@RequestParam String livroId,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {
        UsuarioDetails details = (UsuarioDetails) auth.getPrincipal();
        Reserva reserva = new Reserva();
        reserva.setLivroId(livroId);
        reserva.setUsuarioId(details.getId());
        try {
            reservaService.realizarReserva(reserva);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Reserva realizada! Você será notificado quando o livro estiver disponível.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/catalogo";
    }
}
