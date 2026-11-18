package com.lavamelo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lava-Melo: un solo proceso sirve la API REST (/api/*), el frontend estático (/) y la documentación (/docs).
 */
@SpringBootApplication
public class LavaMeloApplication {

    public static void main(String[] args) {
        SpringApplication.run(LavaMeloApplication.class, args);
    }
}
