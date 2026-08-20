package com.skala.day3.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.day3.repository.OrderRepository;

/**
 * Step 1 — 주문 조회 도구(교안 p.298), 완료 기준 1·2.
 *
 * <p>목적: 모델은 코드를 보지 않는다. {@code description}만 본다. 사용자 ID는 파라미터가 아니라
 * {@link ToolContext}로 받는다(모델이 바꿔 부를 수 없는 통로). 소유자 검증은 이미 완성된
 * {@link OrderRepository#findByIdAndOwnerId}가 강제한다 — 이 메서드만 쓰면 된다.
 *
 * <p>참고: {@code SpringAI_실습/ch09_tools/OrderTools.java}
 *
 * <p>완료 기준:
 * <ul>
 *   <li>1. 주문 질문에 도구가 불린다 — description에 "언제 쓰는지"와 예시 표현을 넣는다</li>
 *   <li>2. 남의 주문은 조회되지 않는다("찾을 수 없습니다"로 응답 — 403이 아니다.
 *       없는 주문과 남의 주문을 구분해 알려주면 그 자체가 정보 노출이다)</li>
 * </ul>
 */
@Component
public class OrderTools {

    private final OrderRepository orders;

    public OrderTools(OrderRepository orders) {
        this.orders = orders;
    }

    @Tool(description = """
            주문번호로 주문 상태와 예상 도착일을 조회한다. 사용자가 주문번호를 명시하면서
            배송 또는 주문 상태를 물을 때만 이 도구를 사용한다. 주문번호가 없는
            '내 주문 어디야', '배송 언제 와' 같은 질문에는 이 도구를 호출하지 말고 주문번호를 먼저 묻는다.
            """)
    public String orderStatus(
            @ToolParam(description = "조회할 주문번호. 예: 12345") String orderId,
            ToolContext context) {

        // userId는 모델이 만드는 도구 인자가 아니라 서버가 넣어 주는 ToolContext에서만 받는다.
        // 따라서 사용자가 프롬프트로 다른 사람의 userId를 주입해도 권한 기준은 바뀌지 않는다.
        String userId = currentUser(context);

        // 주문번호와 소유자를 한 번에 조건으로 조회한다. 없는 주문과 남의 주문에는 같은 문구를
        // 반환하여 주문의 존재 여부까지 노출하지 않고, 예외로 전체 대화를 중단시키지도 않는다.
        return orders.findByIdAndOwnerId(orderId, userId)
                .map(order -> "주문 %s · 품목 %s · 상태 %s · 예상도착 %s"
                        .formatted(order.id(), order.item(), order.status(), order.eta()))
                .orElse("해당 주문을 찾을 수 없습니다.");
    }

    /** 인증 정보가 누락되면 조회를 진행하지 않는 fail-closed 방식으로 처리한다. */
    private String currentUser(ToolContext context) {
        Object userId = context == null ? null : context.getContext().get("userId");
        if (userId == null || userId.toString().isBlank()) {
            throw new IllegalStateException("toolContext에 userId가 없습니다.");
        }
        return userId.toString();
    }
}
