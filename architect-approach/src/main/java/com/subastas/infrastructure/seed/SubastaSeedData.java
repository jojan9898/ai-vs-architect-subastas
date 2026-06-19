package com.subastas.infrastructure.seed;

import com.subastas.domain.Subasta;
import com.subastas.domain.SubastaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Carga datos semilla: una subasta activa "Laptop Gaming" con precio inicial 0.
 *
 * <p>Usa un id fijo y conocido para que sea predecible al probar la API.
 * Es idempotente: si la subasta ya existe, no la vuelve a crear.
 *
 * <p>Se ejecuta en todos los perfiles salvo en test, donde los tests controlan
 * su propio estado.
 */
@Component
@Profile("!test")
public class SubastaSeedData implements CommandLineRunner {

    static final UUID SEED_SUBASTA_ID = UUID.fromString("c0a80001-0000-0000-0000-000000000001");
    private static final Logger log = LoggerFactory.getLogger(SubastaSeedData.class);

    private final SubastaRepository repository;

    public SubastaSeedData(SubastaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.findById(SEED_SUBASTA_ID).isPresent()) {
            log.info("Subasta semilla ya existe: {}", SEED_SUBASTA_ID);
            return;
        }
        Subasta subasta = new Subasta(
                SEED_SUBASTA_ID,
                "Laptop Gaming",
                BigDecimal.ZERO
        );
        repository.save(subasta);
        log.info("Subasta semilla creada -> id={} (POST /subastas/{}/ofertar?monto=X&usuarioId=Y)",
                SEED_SUBASTA_ID, SEED_SUBASTA_ID);
    }
}
