package com.thiago.biblioteca.config;

import com.thiago.biblioteca.model.Usuario;
import com.thiago.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Garante SEMPRE que admin@biblioteca.com existe com senha BCrypt correta.
        // Assim o primeiro acesso sempre funciona, mesmo com dados legados no MongoDB.
        Usuario admin = usuarioRepository.findByEmail("admin@biblioteca.com")
                .orElse(new Usuario());

        admin.setNome("Administrador");
        admin.setEmail("admin@biblioteca.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setRole("BIBLIOTECARIO");
        if (admin.getDocumento() == null) admin.setDocumento("000.000.000-00");
        if (admin.getTelefone() == null)  admin.setTelefone("(00) 0000-0000");
        if (admin.getEndereco() == null)  admin.setEndereco("Biblioteca Municipal");
        usuarioRepository.save(admin);

        System.out.println("==============================================");
        System.out.println("  Admin garantido: admin@biblioteca.com / admin123");
        System.out.println("==============================================");
    }
}
