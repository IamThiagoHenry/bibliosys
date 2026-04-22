package com.thiago.biblioteca.controller;

import com.thiago.biblioteca.model.Usuario;
import com.thiago.biblioteca.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cadastro")
@RequiredArgsConstructor
public class CadastroController {

    private final UsuarioService usuarioService;

    @GetMapping
    public String form() {
        return "cadastro";
    }

    @PostMapping
    public String cadastrar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String confirmarSenha,
            @RequestParam(required = false, defaultValue = "") String telefone,
            @RequestParam(required = false, defaultValue = "") String endereco,
            @RequestParam(required = false, defaultValue = "") String documento,
            Model model) {

        // Validação: senhas coincidem
        if (!senha.equals(confirmarSenha)) {
            model.addAttribute("erroSenha", "As senhas não coincidem.");
            preencherModel(model, nome, email, telefone, endereco, documento);
            return "cadastro";
        }

        // Validação: e-mail único
        if (usuarioService.emailExiste(email)) {
            model.addAttribute("erroEmail", "Este e-mail já está cadastrado. Faça login ou use outro e-mail.");
            preencherModel(model, nome, email, telefone, endereco, documento);
            return "cadastro";
        }

        // Cria usuário com role LEITOR
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);          // UsuarioService.salvar() codifica com BCrypt
        usuario.setTelefone(telefone);
        usuario.setEndereco(endereco);
        usuario.setDocumento(documento);
        usuario.setRole("LEITOR");

        usuarioService.salvar(usuario);

        return "redirect:/login?cadastro=sucesso";
    }

    /** Repõe os valores no modelo para manter o formulário preenchido após erro */
    private void preencherModel(Model model, String nome, String email,
                                String telefone, String endereco, String documento) {
        model.addAttribute("nome", nome);
        model.addAttribute("email", email);
        model.addAttribute("telefone", telefone);
        model.addAttribute("endereco", endereco);
        model.addAttribute("documento", documento);
    }
}
