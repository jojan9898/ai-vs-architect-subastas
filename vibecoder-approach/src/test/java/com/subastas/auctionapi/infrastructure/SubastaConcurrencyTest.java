package com.subastas.auctionapi.infrastructure;

import com.subastas.auctionapi.application.HacerOfertaUseCase;
import com.subastas.auctionapi.domain.Subasta;
import com.subastas.auctionapi.domain.SubastaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SubastaConcurrencyTest {

    @Autowired
    HacerOfertaUseCase hacerOfertaUseCase;

    @Autowired
    SubastaRepository repository;

    @Test
    void only_one_bid_wins_under_concurrency() throws InterruptedException {
        UUID subastaId = UUID.randomUUID();
        repository.save(new Subasta(subastaId, "Laptop Gaming"));

        int threads = 50;
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger wins = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            UUID usuarioId = UUID.randomUUID();
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    hacerOfertaUseCase.ejecutar(subastaId, BigDecimal.ONE, usuarioId);
                    wins.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }

        ready.await();
        start.countDown();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        assertThat(wins.get())
                .as("exactly one bid must win under concurrent contention")
                .isEqualTo(1);

        Subasta subasta = repository.findById(subastaId).orElseThrow();
        assertThat(subasta.getMontoMasAlto()).isEqualByComparingTo(BigDecimal.ONE);
    }
}
