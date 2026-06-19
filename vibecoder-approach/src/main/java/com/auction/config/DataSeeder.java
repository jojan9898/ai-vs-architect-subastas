package com.auction.config;

import com.auction.domain.Auction;
import com.auction.repository.AuctionRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    ApplicationRunner seedAuctions(AuctionRepository auctionRepository) {
        return args -> {
            if (auctionRepository.findByTitle("Laptop Gaming").isEmpty()) {
                auctionRepository.save(new Auction("Laptop Gaming"));
            }
        };
    }
}
