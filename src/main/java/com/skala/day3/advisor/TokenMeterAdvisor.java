package com.skala.day3.advisor;

import java.util.concurrent.TimeUnit;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Step 6 — 토큰·지연 계측(교안 p.303), 완료 기준 8.
 *
 * <p>목적: 토큰·지연을 계측한다. 태그를 붙여야 기능별로 쪼개 볼 수 있다.
 *
 * <p>참고: {@code SpringAI_실습/ch11_advisors/TokenMeterAdvisor.java}
 *
 * <p>완료 기준 8: {@code GET /actuator/metrics/ai.tokens}·{@code ai.latency}가 쌓인다.
 *
 * <p>⚠️ 시간이 모자라면 8개 중 가장 먼저 빼도 되는 항목이다(9개 중 7개면 목표 달성).
 */
@Component
public class TokenMeterAdvisor implements CallAdvisor {

    private final MeterRegistry registry;

    public TokenMeterAdvisor(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 자신보다 안쪽의 Advisor와 모델 호출 전체 시간을 재고, 모델이 반환한 Usage를 누적한다.
     * {@code finally}에서 시간을 기록하므로 모델 호출이 실패해도 지연 관측값은 남는다.
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long started = System.nanoTime();

        try {
            ChatClientResponse response = chain.nextCall(request);
            recordTokens(response);
            return response;
        } finally {
            registry.timer("ai.latency", "phase", "model")
                    .record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
        }
    }

    /** 모델 공급자가 Usage를 제공한 경우에만 입력·출력 토큰을 기능별 Counter에 더한다. */
    private void recordTokens(ChatClientResponse response) {
        if (response == null || response.chatResponse() == null) {
            return;
        }

        Usage usage = response.chatResponse().getMetadata().getUsage();
        if (usage == null) {
            return;
        }

        int promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens() : 0;
        int completionTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens() : 0;
        registry.counter("ai.tokens", "type", "prompt", "feature", "chat")
                .increment(promptTokens);
        registry.counter("ai.tokens", "type", "completion", "feature", "chat")
                .increment(completionTokens);
    }

    @Override
    public String getName() {
        return "tokenMeter";
    }

    /** 낮을수록 바깥 — 계측은 바깥쪽에 둬야 안쪽 전체 시간이 잡힌다. */
    @Override
    public int getOrder() {
        return 10;
    }
}
