package com.skala.day3.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

/**
 * TODO ⑦이 끝났는지 확인하는 테스트 — 완료 기준 5(세션 격리).
 * conversationId 규칙이 한 곳에서만 만들어지는지, 사용자·세션이 다르면 값이 다른지 확인한다.
 *
 * <p>ChatClient는 이 테스트에서 쓰지 않으므로(모델을 호출하지 않는다) null로 둔다 —
 * Spring 컨텍스트를 띄우지 않는 순수 단위 테스트라 TODO ⑥(ChatClient 조립) 완료 여부와
 * 무관하게 TODO ⑦만 끝나면 통과한다.
 */
class ConversationIdTest {

    private final ConsultService consultService =
            new ConsultService(null, MessageWindowChatMemory.builder()
                    .chatMemoryRepository(new InMemoryChatMemoryRepository())
                    .build());

    @Test
    void 같은_사용자_같은_세션이면_동일하다() {
        assertThat(consultService.conversationId("user1", "s1"))
                .isEqualTo(consultService.conversationId("user1", "s1"));
    }

    @Test
    void 세션이_다르면_ID가_다르다() {
        assertThat(consultService.conversationId("user1", "s1"))
                .isNotEqualTo(consultService.conversationId("user1", "s2"));
    }

    @Test
    void 사용자가_다르면_ID가_다르다() {
        assertThat(consultService.conversationId("user1", "s1"))
                .isNotEqualTo(consultService.conversationId("user2", "s1"));
    }
}
