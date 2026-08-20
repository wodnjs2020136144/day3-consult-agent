package com.skala.day3.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * ★ TODO ⑤ — Step 6 (교안 p.303), 완료 기준 8.
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

    // TODO ⑤: chain.nextCall(request)로 다음 Advisor/모델을 호출하고, 걸린 시간을
    //          registry.timer("ai.latency")에 기록한다. 응답의 Usage(promptTokens·
    //          completionTokens)를 registry.counter("ai.tokens", "type", "prompt"/"completion")에
    //          더한다.
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        throw new UnsupportedOperationException("TODO ⑤: TokenMeterAdvisor.adviseCall 을 구현하세요");
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
