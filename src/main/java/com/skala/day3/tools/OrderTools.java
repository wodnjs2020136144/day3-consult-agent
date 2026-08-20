package com.skala.day3.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.skala.day3.repository.OrderRepository;

/**
 * ★ TODO ① — Step 1 (교안 p.298), 완료 기준 1·2.
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

    // TODO ①: @Tool description을 채운다 — "주문 상태를 조회한다. 사용자가 주문번호를 말하거나
    //          '내 주문', '배송 언제' 처럼 물으면 이 도구를 쓴다." 같은 형태.
    // TODO ①: @ToolParam description에 orderId 예시("예: 12345")를 넣는다.
    // TODO ①: userId를 ToolContext에서 꺼내 orders.findByIdAndOwnerId(orderId, userId)로 조회하고,
    //          없으면 "해당 주문을 찾을 수 없습니다." 같은 안전한 실패 문구를 반환한다(예외를 던지지 않는다).
    @Tool(description = "TODO: 여기에 도구 설명을 채운다")
    public String orderStatus(@ToolParam(description = "TODO") String orderId, ToolContext context) {
        throw new UnsupportedOperationException("TODO ①: OrderTools.orderStatus 를 구현하세요");
    }
}
