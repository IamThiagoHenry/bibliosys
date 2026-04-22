package com.thiago.biblioteca.repository;

import com.thiago.biblioteca.model.Suporte;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SuporteRepository extends MongoRepository<Suporte, String> {
    List<Suporte> findByStatus(String status);
    List<Suporte> findByEmailIgnoreCaseOrderByDataCriacaoDesc(String email);
}
