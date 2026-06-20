package com.subastas.auctionapi.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubastaRepository {

    Optional<Subasta> findById(UUID id);

    List<Subasta> findAll();

    Subasta save(Subasta subasta);
}
