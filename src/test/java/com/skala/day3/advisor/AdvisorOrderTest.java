package com.skala.day3.advisor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

/**
 * TODO ④가 끝났는지 확인하는 테스트 — 완료 기준 6(Advisor 순서).
 * "차단이 메모리보다 앞이다"를 코드로 못 박는다(교안 p.294 예시 그대로).
 *
 * <p>{@link SafetyAdvisor#getOrder()}는 이미 100으로 완성돼 있다 — 이 테스트는
 * TODO ④의 {@code before()} 구현 여부와 무관하게 순서 값만 검증하므로, 값을
 * 그대로 두면(또는 실수로 250으로 바꾸면) 곧바로 결과가 바뀐다.
 */
class AdvisorOrderTest {

    @Test
    void 차단이_메모리보다_앞이다() {
        SafetyAdvisor safety = new SafetyAdvisor();
        MessageChatMemoryAdvisor memory = MessageChatMemoryAdvisor.builder(
                        MessageWindowChatMemory.builder()
                                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                                .build())
                .order(200)
                .build();

        assertThat(safety.getOrder()).isLessThan(memory.getOrder());
    }
}
