package com.thiago.biblioteca.service;

import com.thiago.biblioteca.model.Suporte;
import com.thiago.biblioteca.repository.SuporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SuporteService {

    private final SuporteRepository suporteRepository;

    public List<Suporte> listarTodos() {
        return suporteRepository.findAll();
    }

    public Optional<Suporte> buscarPorId(String id) {
        return suporteRepository.findById(id);
    }

    public List<Suporte> listarPorEmail(String email) {
        return suporteRepository.findByEmailIgnoreCaseOrderByDataCriacaoDesc(email);
    }

    public Suporte salvar(Suporte suporte) {
        if (suporte.getId() != null && suporte.getId().isBlank()) {
            suporte.setId(null); // força MongoDB a gerar ObjectId
        }
        if (suporte.getId() == null) {
            suporte.setDataCriacao(LocalDateTime.now());
            suporte.setStatus("ABERTO");
        }
        return suporteRepository.save(suporte);
    }

    public Suporte responder(String id, String resposta) {
        Suporte suporte = suporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado: " + id));
        suporte.setResposta(resposta);
        suporte.setDataResposta(LocalDateTime.now());
        suporte.setStatus("RESOLVIDO");
        return suporteRepository.save(suporte);
    }

    public Suporte resolver(String id) {
        Suporte suporte = suporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado: " + id));
        suporte.setStatus("RESOLVIDO");
        return suporteRepository.save(suporte);
    }

    public Suporte avaliar(String id, Integer estrelas, String reacao) {
        Suporte suporte = suporteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado: " + id));
        suporte.setAvaliacaoEstrelas(estrelas);
        suporte.setAvaliacaoReacao(reacao);
        return suporteRepository.save(suporte);
    }

    public void deletar(String id) {
        suporteRepository.deleteById(id);
    }
}
