package com.skala.day3.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.day3.domain.Ticket;
import com.skala.day3.repository.OrderRepository;
import com.skala.day3.repository.TicketRepository;

/**
 * ★ TODO ② — Step 3 (교안 p.300), 완료 기준 3.
 *
 * <p>목적: 되돌리기 어려운 행동(환불)은 접수까지만 도구에 준다. 실행 버튼은 사람이 누른다
 * — 그 경로는 {@code AdminController}에 있고 도구 목록에는 없어서 모델이 닿을 수 없다.
 *
 * <p>참고: {@code SpringAI_실습/11_승인게이트/SnackTools.java}
 *
 * <p>완료 기준 3: 환불이 접수(PENDING)로만 남는다 — 즉시 처리되면 실패다.
 */
@Component
public class RefundTools {

    private final OrderRepository orders;
    private final TicketRepository tickets;

    public RefundTools(OrderRepository orders, TicketRepository tickets) {
        this.orders = orders;
        this.tickets = tickets;
    }

    @Tool(description = "환불을 접수한다. 사용자가 환불·반품·교환을 요청하면 이 도구를 쓴다. "
            + "이 도구는 접수까지만 하며 즉시 처리되지 않는다 — 담당자 승인 후에 처리된다.")
    public String requestRefund(@ToolParam(description = "주문번호. 예: 12345") String orderId,
                                @ToolParam(description = "환불 사유. 예: 단순 변심") String reason,
                                ToolContext context) {
        String userId = (String) context.getContext().get("userId");
        return orders.findByIdAndOwnerId(orderId, userId)
                .map(o -> {
                    Ticket ticket = tickets.create(orderId, userId, reason);
                    return "환불 접수가 완료되었습니다(티켓번호 %s). 담당자 승인 후 처리됩니다."
                            .formatted(ticket.no());
                })
                .orElse("해당 주문을 찾을 수 없습니다.");
    }
}
