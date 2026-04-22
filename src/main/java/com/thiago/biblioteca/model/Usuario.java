package com.thiago.biblioteca.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;
    private String nome;
    private String endereco;
    private String telefone;
    private String email;
    private String documento;
    private String senha;
    private String role; // LEITOR ou BIBLIOTECARIO
}