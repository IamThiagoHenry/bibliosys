package com.thiago.biblioteca.service;

import com.thiago.biblioteca.model.Usuario;
import com.thiago.biblioteca.model.UsuarioDetails;
import com.thiago.biblioteca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
        return new UsuarioDetails(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() != null && usuario.getId().isBlank()) {
            usuario.setId(null); // força MongoDB a gerar ObjectId
        }
        if (usuario.getId() == null) {
            // Novo usuário — obrigatório ter senha
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        } else {
            // Edição — manter senha antiga se o campo vier vazio
            if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                usuarioRepository.findById(usuario.getId())
                        .ifPresent(existente -> usuario.setSenha(existente.getSenha()));
            } else {
                usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
            }
        }
        return usuarioRepository.save(usuario);
    }

    public boolean emailExiste(String email) {
        return usuarioRepository.findByEmail(email).isPresent();
    }

    public void deletar(String id) {
        usuarioRepository.deleteById(id);
    }
}
