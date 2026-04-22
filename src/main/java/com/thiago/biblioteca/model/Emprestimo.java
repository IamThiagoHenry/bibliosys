package com.thiago.biblioteca.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@Document(collection = "emprestimos")
public class Emprestimo {

    @Id
    private String id;
    private String usuarioId;
    private String livroId;
    private String nomeUsuario;
    private String tituloLivro;
    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucao;    // prazo (due date)
    private LocalDate dataRetornoReal;  // data real de entrega pelo leitor
    private String status; // "ATIVO", "DEVOLVIDO", "ATRASADO"
    private Double multa;

    /** Calculado em tempo de execução — não persiste no MongoDB */
    @Transient
    private boolean vencendoEmBreve;
}
