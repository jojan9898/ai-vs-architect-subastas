package com.auctionbackend.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for placing a bid. Only format-level validation lives here
 * (non-null amount, non-blank user). The "must be strictly higher" rule is a
 * business invariant and stays in the domain aggregate.
 */
public record BidRequest(@NotNull BigDecimal amount, @NotBlank String userId) {
}
