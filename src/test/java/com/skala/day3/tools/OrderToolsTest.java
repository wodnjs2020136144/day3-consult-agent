package com.skala.day3.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;

import com.skala.day3.repository.OrderRepository;

/**
 * TODO ①이 끝났는지 확인하는 테스트 — 완료 기준 1·2.
 * 모델 없이 도구 메서드를 직접 호출해 검증한다.
 */
class OrderToolsTest {

    private final OrderTools tools = new OrderTools(new OrderRepository());

    @Test
    void 본인_주문은_조회된다() {
        String result = tools.orderStatus("12345", ctx("user1"));
        assertThat(result).contains("12345");
    }

    @Test
    void 남의_주문은_조회되지_않는다() {
        // 99999는 user2 소유 — user1이 조회하면 찾을 수 없어야 한다(403이 아니다)
        String result = tools.orderStatus("99999", ctx("user1"));
        assertThat(result).contains("찾을 수 없습니다");
    }

    @Test
    void 존재하지_않는_주문도_같은_문구다() {
        // 없는 주문과 남의 주문을 구분해 알려주면 그 자체가 정보 노출이다
        String 없는주문 = tools.orderStatus("00000", ctx("user1"));
        String 남의주문 = tools.orderStatus("99999", ctx("user1"));
        assertThat(없는주문).isEqualTo(남의주문);
    }

    private ToolContext ctx(String userId) {
        return new ToolContext(Map.of("userId", userId));
    }
}
