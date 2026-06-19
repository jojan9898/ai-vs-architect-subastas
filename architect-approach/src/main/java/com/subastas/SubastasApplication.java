package com.subastas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion.
 *
 * <p>No declara {@code @EntityScan} ni {@code @EnableJpaRepositories} a proposito:
 * Spring Boot autodetecta entidades y repositorios en el paquete base
 * {@code com.subastas} y sus subpaquetes, y asi los tests de slice web
 * ({@code @WebMvcTest}) no arrastran infraestructura JPA que no necesitan.
 */
@SpringBootApplication
public class SubastasApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubastasApplication.class, args);
    }
}
