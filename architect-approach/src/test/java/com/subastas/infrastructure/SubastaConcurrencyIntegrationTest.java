package com.subastas.infrastructure;

import com.subastas.domain.Oferta;
import com.subastas.domain.Subasta;
import com.subastas.domain.SubastaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test de integracion que verifica el optimistic locking real (@Version) contra
 * una base H2 en memoria.
 *
 * <p>Simula la carrera entre dos usuarios: ambos cargan la subasta en la misma
 * version V, el usuario A gana la carrera (commits, version -> V+1) y el
 * usuario B, que tiene la version V, debe recibir un
 * {@link OptimisticLockingFailureException} al intentar guardar. Esto es la
 * base del HTTP 409 Conflict.
 *
 * <p>Se usan transacciones programaticas con {@link TransactionTemplate} y
 * propagacion REQUIRES_NEW para aislar carga y guardado en transacciones
 * separadas, reproduciendo el ciclo load-then-save de dos clientes distintos.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:subastas;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class SubastaConcurrencyIntegrationTest {

    @Autowired
    private SubastaRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("dos ofertas concurrentes sobre la misma version: el perdedor recibe conflicto")
    void conflictoOptimisticLockingCuandoDosGuardanLaMismaVersion() {
        UUID subastaId = UUID.randomUUID();
        TransactionTemplate tx = newTransactionTemplate();

        // Crea la subasta inicial (version 0 tras el primer save)
        tx.executeWithoutResult(status ->
                repository.save(new Subasta(subastaId, "Laptop Gaming", new BigDecimal("0"))));

        // Usuario A y B cargan en sus propias transacciones: ambos ven version 1
        Subasta snapshotA = tx.execute(status -> repository.findById(subastaId).orElseThrow());
        Subasta snapshotB = tx.execute(status -> repository.findById(subastaId).orElseThrow());
        assertThat(snapshotA.getVersion()).isEqualTo(snapshotB.getVersion());

        // Usuario A gana la carrera: ofrece 100 y commitea (version -> 2)
        snapshotA.hacerOferta(new Oferta(new BigDecimal("100"), UUID.randomUUID(), Instant.now()));
        Subasta ganada = tx.execute(status -> repository.save(snapshotA));
        assertThat(ganada.getVersion()).isEqualTo(snapshotA.getVersion() + 1);

        // Usuario B (con la version vieja) intenta guardar: debe fallar con conflicto
        snapshotB.hacerOferta(new Oferta(new BigDecimal("50"), UUID.randomUUID(), Instant.now()));
        assertThatThrownBy(() -> tx.execute(status -> repository.save(snapshotB)))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("dos ofertas secuenciales (sin solaparse) no producen conflicto")
    void ofertasSecuencialesNoFallan() {
        UUID subastaId = UUID.randomUUID();
        TransactionTemplate tx = newTransactionTemplate();

        tx.executeWithoutResult(status ->
                repository.save(new Subasta(subastaId, "Laptop Gaming", new BigDecimal("0"))));

        tx.execute(status -> {
            Subasta s = repository.findById(subastaId).orElseThrow();
            s.hacerOferta(new Oferta(new BigDecimal("100"), UUID.randomUUID(), Instant.now()));
            return repository.save(s);
        });

        Subasta finalState = tx.execute(status -> {
            Subasta s = repository.findById(subastaId).orElseThrow();
            s.hacerOferta(new Oferta(new BigDecimal("200"), UUID.randomUUID(), Instant.now()));
            return repository.save(s);
        });

        assertThat(finalState.getOfertaMasAlta().monto()).isEqualByComparingTo("200");
    }

    private TransactionTemplate newTransactionTemplate() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        return tx;
    }
}
