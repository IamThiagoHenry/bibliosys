package com.thiago.biblioteca.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "suporte")
public class Suporte {

    @Id
    private String id;
    private String nome;
    private String email;
    private String tipo; // DUVIDA, RECLAMACAO, SUGESTAO
    private String mensagem;
    private LocalDateTime dataCriacao;
    private String status; // ABERTO, RESOLVIDO
    private String resposta;
    private LocalDateTime dataResposta;
    private Integer avaliacaoEstrelas; // 1 a 5
    private String avaliacaoReacao;    // CURTIU, NEUTRO, NAO_CURTIU
}
