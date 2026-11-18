package com.lavamelo.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Elige la implementación del repositorio con la variable de entorno REPO=memory|sql (default memory). */
@Configuration
public class RepositorioConfig {

    private static final Logger log = LoggerFactory.getLogger(RepositorioConfig.class);

    @Bean
    public Repositorio repositorio(@Value("${REPO:memory}") String tipo) {
        log.info("Repositorio seleccionado: REPO={}", tipo);
        return switch (tipo.trim().toLowerCase()) {
            case "memory" -> new MemoryRepositorio();
            case "sql" -> new SqlRepositorio();
            default -> throw new IllegalArgumentException("REPO debe ser 'memory' o 'sql', no '" + tipo + "'");
        };
    }
}
