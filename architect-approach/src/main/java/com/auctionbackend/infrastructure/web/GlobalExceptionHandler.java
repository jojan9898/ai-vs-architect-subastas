package com.auctionbackend.infrastructure.web;

import java.util.stream.Collectors;

import jakarta.persistence.OptimisticLockException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.auctionbackend.domain.exception.AuctionNotFoundException;
import com.auctionbackend.domain.exception.BidTooLowException;

/**
 * Maps domain and infrastructure exceptions to HTTP responses.
 *
 * <p>Two outcomes that must never be confused:
 * <ul>
 *   <li>400 — the request violated a business rule (bid too low).</li>
 *   <li>409 — a concurrent transaction won the race; the client may retry.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BidTooLowException.class)
    ResponseEntity<ErrorResponse> handleBidTooLow(BidTooLowException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("BID_TOO_LOW", ex.getMessage()));
    }

    @ExceptionHandler(AuctionNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(AuctionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("AUCTION_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler({
            ObjectOptimisticLockingFailureException.class,
            org.springframework.dao.OptimisticLockingFailureException.class,
            OptimisticLockException.class
    })
    ResponseEntity<ErrorResponse> handleConcurrencyConflict(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONCURRENCY_CONFLICT",
                        "The auction was modified by another transaction. Please retry."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("INVALID_ARGUMENT", ex.getMessage()));
    }
}
