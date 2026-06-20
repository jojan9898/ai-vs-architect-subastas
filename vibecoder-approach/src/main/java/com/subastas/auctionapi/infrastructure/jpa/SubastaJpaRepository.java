package com.subastas.auctionapi.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SubastaJpaRepository extends JpaRepository<SubastaEntity, UUID> {
}
