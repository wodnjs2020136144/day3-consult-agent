package com.skala.day3.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

import com.skala.day3.domain.Ticket;
import com.skala.day3.domain.TicketStatus;

/**
 * 환불 티켓 저장소. 완성 상태로 제공된다 — 손대지 않는다.
 *
 * <p>접수({@link #create})와 승인({@link #approve})을 분리하는 것이 이 실습의 핵심이다.
 * {@code create}는 도구(모델이 닿을 수 있는 경로)에서 부르고, {@code approve}는
 * {@code AdminController}(모델이 닿을 수 없는 경로)에서만 부른다.
 */
@Repository
public class TicketRepository {

    private final List<Ticket> tickets = new ArrayList<>();
    private final AtomicInteger seq = new AtomicInteger(1);

    public synchronized Ticket create(String orderId, String userId, String reason) {
        Ticket ticket = new Ticket("T-%04d".formatted(seq.getAndIncrement()),
                orderId, userId, reason, TicketStatus.PENDING);
        tickets.add(ticket);
        return ticket;
    }

    public synchronized List<Ticket> pending() {
        return tickets.stream().filter(t -> t.status() == TicketStatus.PENDING).toList();
    }

    public synchronized Optional<Ticket> approve(String no) {
        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);
            if (t.no().equals(no)) {
                Ticket approved = t.approve();
                tickets.set(i, approved);
                return Optional.of(approved);
            }
        }
        return Optional.empty();
    }

    public synchronized Optional<Ticket> find(String no) {
        return tickets.stream().filter(t -> t.no().equals(no)).findFirst();
    }
}
