package com.skala.day3.domain;

public record Ticket(String no, String orderId, String userId, String reason, TicketStatus status) {

    public Ticket approve() {
        return new Ticket(no, orderId, userId, reason, TicketStatus.APPROVED);
    }
}
