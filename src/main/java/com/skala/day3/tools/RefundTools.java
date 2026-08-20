package com.skala.day3.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.day3.domain.Ticket;
import com.skala.day3.repository.OrderRepository;
import com.skala.day3.repository.TicketRepository;

/**
 * Step 3 — 환불 승인 게이트(교안 p.300), 완료 기준 3.
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

    @Tool(description = """
            환불 요청을 접수한다. 실제 환불을 즉시 처리하지 않으며 담당자 승인 후 처리된다.
            사용자가 특정 주문의 환불을 명시적으로 요청했을 때만 이 도구를 사용한다.
            """)
    public String requestRefund(
            @ToolParam(description = "환불할 주문번호. 예: 12345") String orderId,
            @ToolParam(description = "사용자가 말한 환불 사유. 예: 단순 변심") String reason,
            ToolContext context) {
        // 사용자가 프롬프트에 적은 ID가 아니라 서버가 ToolContext에 넣은 ID만 권한 기준으로 쓴다.
        String userId = currentUser(context);

        // 티켓을 만들기 전에 주문 소유권부터 확인한다. 없는 주문과 남의 주문은 같은 문구로 처리한다.
        if (orders.findByIdAndOwnerId(orderId, userId).isEmpty()) {
            return "해당 주문을 찾을 수 없습니다.";
        }

        // create()는 PENDING 티켓만 만든다. 실제 승인은 도구가 아닌 AdminController에만 존재한다.
        Ticket ticket = tickets.create(orderId, userId, reason);
        return "환불 요청 %s번으로 접수했습니다. 담당자 승인 후 처리됩니다."
                .formatted(ticket.no());
    }

    /** 인증 정보가 누락되면 환불 접수를 진행하지 않는다. */
    private String currentUser(ToolContext context) {
        Object userId = context == null ? null : context.getContext().get("userId");
        if (userId == null || userId.toString().isBlank()) {
            throw new IllegalStateException("toolContext에 userId가 없습니다.");
        }
        return userId.toString();
    }
}
