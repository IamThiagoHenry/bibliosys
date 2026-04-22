package com.thiago.biblioteca.controller;

import com.thiago.biblioteca.model.Usuario;
import com.thiago.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/esqueci-senha")
@RequiredArgsConstructor
public class EsqueciSenhaController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SENHA_TEMPORARIA = "Biblioteca@123";

    @GetMapping
    public String form() {
        return "esqueci-senha";
    }

    @PostMapping
    public String processar(@RequestParam String email, Model model) {
        Optional<Usuario> opt = usuarioRepository.findByEmail(email);

        if (opt.isEmpty()) {
            model.addAttribute("erro", "E-mail não encontrado. Verifique o endereço ou cadastre-se.");
            model.addAttribute("email", email);
            return "esqueci-senha";
        }

        // Sem servidor de e-mail: define senha temporária e exibe na tela
        Usuario usuario = opt.get();
        usuario.setSenha(passwordEncoder.encode(SENHA_TEMPORARIA));
        usuarioRepository.save(usuario);

        model.addAttribute("sucesso", true);
        model.addAttribute("senhaTemp", SENHA_TEMPORARIA);
        model.addAttribute("nomeUsuario", usuario.getNome());
        return "esqueci-senha";
    }
}
