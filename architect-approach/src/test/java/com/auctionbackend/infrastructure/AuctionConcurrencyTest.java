package com.auctionbackend.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.auctionbackend.application.PlaceBidUseCase;
import com.auctionbackend.domain.Auction;
import com.auctionbackend.domain.AuctionRepository;

/**
 * Proves the optimistic-lock guarantee: when many threads bid the SAME amount
 * on a fresh auction, exactly one bid may win. Without {@code @Version} several
 * would silently overwrite each other (lost update) and {@code wins} would be
 * greater than one. With it, the losers get a 409-grade conflict (visible, not
 * silent) and the invariant — highest bid equals the winning amount — holds.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuctionConcurrencyTest {

    @Autowired
    PlaceBidUseCase placeBidUseCase;

    @Autowired
    AuctionRepository auctionRepository;

    @Test
    void only_one_bid_wins_under_concurrency() throws Exception {
        UUID auctionId = UUID.randomUUID();
        auctionRepository.save(new Auction(auctionId, "Concurrency Target"));

        int threads = 32;
        BigDecimal amount = new BigDecimal("100");
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);
        AtomicInteger wins = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            String userId = "user-" + i;
            pool.submit(() -> {
                try {
                    startGate.await();
                    placeBidUseCase.execute(auctionId, amount, userId);
                    wins.incrementAndGet();
                } catch (Exception ignored) {
                    // losers: version conflict (409) or bid too low (400) — both expected
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        boolean finished = doneGate.await(60, TimeUnit.SECONDS);
        pool.shutdownNow();
        assertTrue(finished, "all threads must finish within the timeout");

        assertThat(wins.get())
                .as("exactly one concurrent bid should win; others must be rejected")
                .isEqualTo(1);

        Auction refreshed = auctionRepository.findById(auctionId).orElseThrow();
        assertThat(refreshed.currentHighestAmount()).isEqualByComparingTo(amount);
    }
}
