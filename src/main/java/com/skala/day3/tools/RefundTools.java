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

    // TODO ②: @Tool description을 채운다 — "환불을 접수한다. 즉시 처리되지 않고 담당자 승인 후
    //          처리된다." 같은 형태(모델이 "내가 바로 처리했다"고 착각하지 않게 설명에 명시한다).
    // TODO ②: userId를 ToolContext에서 꺼내 orders.findByIdAndOwnerId(orderId, userId)로
    //          권한을 먼저 확인한다(없으면 "해당 주문을 찾을 수 없습니다." 반환) — 남의 주문으로
    //          환불 티켓을 만들 수 없어야 한다.
    // TODO ②: 권한 확인을 통과하면 tickets.create(orderId, userId, reason)으로 접수(PENDING)만 하고,
    //          티켓 번호와 "담당자 승인 후 처리됩니다." 안내를 함께 반환한다.
    @Tool(description = "TODO: 여기에 도구 설명을 채운다")
    public String requestRefund(@ToolParam(description = "TODO") String orderId,
                                @ToolParam(description = "TODO") String reason,
                                ToolContext context) {
        throw new UnsupportedOperationException("TODO ②: RefundTools.requestRefund 를 구현하세요");
    }
}
