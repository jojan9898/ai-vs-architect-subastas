package com.subastas.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provee un {@link Clock} del sistema (UTC) para que los casos de uso sean
 * deterministas y testeables: en tests puede inyectarse un Clock fijo.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
