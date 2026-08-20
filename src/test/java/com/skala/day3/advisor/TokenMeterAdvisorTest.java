package com.skala.day3.advisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

/** Step 6 계측 Advisor가 모델 Usage와 호출 지연을 Micrometer에 기록하는지 검증한다. */
class TokenMeterAdvisorTest {

    @Test
    void 프롬프트와_완성_토큰_및_지연을_기록한다() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        TokenMeterAdvisor advisor = new TokenMeterAdvisor(registry);
        ChatClientRequest request = new ChatClientRequest(new Prompt("안녕하세요"), Map.of());
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatResponseMetadata metadata = ChatResponseMetadata.builder()
                .usage(new DefaultUsage(12, 7))
                .build();
        ChatResponse chatResponse = new ChatResponse(
                List.of(new Generation(new AssistantMessage("반갑습니다"))), metadata);
        ChatClientResponse expected = new ChatClientResponse(chatResponse, Map.of());
        given(chain.nextCall(request)).willReturn(expected);

        ChatClientResponse actual = advisor.adviseCall(request, chain);

        assertThat(actual).isSameAs(expected);
        assertThat(registry.get("ai.tokens")
                .tags("type", "prompt", "feature", "chat")
                .counter().count()).isEqualTo(12.0);
        assertThat(registry.get("ai.tokens")
                .tags("type", "completion", "feature", "chat")
                .counter().count()).isEqualTo(7.0);
        assertThat(registry.get("ai.latency")
                .tag("phase", "model")
                .timer().count()).isEqualTo(1L);
        assertThat(advisor.getOrder()).isEqualTo(10);
    }
}
