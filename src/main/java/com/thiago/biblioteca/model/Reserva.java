package com.thiago.biblioteca.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Document(collection = "reservas")
public class Reserva {

    @Id
    private String id;
    private String usuarioId;
    private String livroId;
    private String tituloLivro;
    private String nomeUsuario;
    private LocalDate dataReserva;
    private String status; // AGUARDANDO, CONCLUIDA, CANCELADA
    private boolean disponivelParaRetirada;
    private LocalDateTime dataNotificacao; // quando disponivelParaRetirada foi marcado (para controle das 48h)
}
