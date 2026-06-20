package com.subastas.auctionapi.application;

import com.subastas.auctionapi.domain.Subasta;
import com.subastas.auctionapi.domain.SubastaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BuscarSubastasUseCase {

    private final SubastaRepository repository;

    public BuscarSubastasUseCase(SubastaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Subasta> listar() {
        return repository.findAll();
    }
}
