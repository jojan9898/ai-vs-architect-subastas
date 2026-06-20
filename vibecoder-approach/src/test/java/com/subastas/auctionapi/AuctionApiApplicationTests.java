package com.subastas.auctionapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuctionApiApplicationTests {

    @Autowired
    ApplicationContext applicationContext;

    @Test
    void context_loads() {
        assertThat(applicationContext).isNotNull();
    }
}
