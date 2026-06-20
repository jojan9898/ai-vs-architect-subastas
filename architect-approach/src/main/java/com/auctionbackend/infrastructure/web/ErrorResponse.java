package com.auctionbackend.infrastructure.web;

/**
 * Uniform error body: a stable machine-readable code plus a human-readable
 * message. Lets clients distinguish "your bid broke the rule" (BID_TOO_LOW,
 * 400) from "you lost a race, retry" (CONCURRENCY_CONFLICT, 409).
 */
public record ErrorResponse(String code, String message) {
}
