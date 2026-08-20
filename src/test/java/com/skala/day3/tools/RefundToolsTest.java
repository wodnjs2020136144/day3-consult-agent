package com.skala.day3.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;

import com.skala.day3.domain.TicketStatus;
import com.skala.day3.repository.OrderRepository;
import com.skala.day3.repository.TicketRepository;

/**
 * TODO ②가 끝났는지 확인하는 테스트 — 완료 기준 3.
 * "즉시 처리됨"이 아니라 "접수까지만" 되는지가 핵심이다.
 */
class RefundToolsTest {

    private final OrderRepository orders = new OrderRepository();
    private final TicketRepository tickets = new TicketRepository();
    private final RefundTools tools = new RefundTools(orders, tickets);

    @Test
    void 환불은_접수까지만_된다() {
        String result = tools.requestRefund("12345", "단순 변심", ctx("user1"));

        assertThat(result).contains("접수");
        assertThat(tickets.pending())
                .anySatisfy(t -> assertThat(t.status()).isEqualTo(TicketStatus.PENDING));
    }

    @Test
    void 남의_주문은_환불_접수도_안된다() {
        String result = tools.requestRefund("99999", "그냥", ctx("user1"));

        assertThat(result).contains("찾을 수 없습니다");
        assertThat(tickets.pending()).isEmpty();
    }

    private ToolContext ctx(String userId) {
        return new ToolContext(Map.of("userId", userId));
    }
}
