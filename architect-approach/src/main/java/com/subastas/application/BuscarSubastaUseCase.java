package com.subastas.application;

import com.subastas.domain.Subasta;
import com.subastas.domain.SubastaRepository;
import com.subastas.domain.exception.SubastaNoEncontradaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Caso de uso: obtener una subasta por su identificador (lado de lectura).
 */
@Service
public class BuscarSubastaUseCase {

    private final SubastaRepository repository;

    public BuscarSubastaUseCase(SubastaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Subasta ejecutar(UUID subastaId) {
        return repository.findById(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException(subastaId));
    }
}
