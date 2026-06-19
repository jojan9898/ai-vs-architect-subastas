package com.subastas.infrastructure.controllers.dto;

/**
 * DTO de error estandar para todas las respuestas no exitosas.
 */
public record ErrorResponse(String code, String message) {
}
