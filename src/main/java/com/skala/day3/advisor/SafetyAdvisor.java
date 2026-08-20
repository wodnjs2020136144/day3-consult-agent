package com.skala.day3.advisor;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;

/**
 * Step 4 — Safety Advisor(교안 p.301), 완료 기준 6.
 *
 * <p>목적: 프롬프트 인젝션·민감어 요청을 대화 메모리에 저장되기 <b>전</b>에 차단한다.
 * 그래서 이 Advisor의 {@code order}는 메모리 Advisor보다 반드시 앞(작은 값)이어야 한다
 * — {@link Day3AiConfig}에서 실제로 조립할 때 이 순서를 지킨다.
 *
 * <p>참고: {@code SpringAI_실습/ch11_advisors/MemoryChatConfig.java}의 {@code SafeGuardAdvisor}
 * 사용 패턴, {@code SpringAI_실습/12_Advisor순서/이모지Advisor.java}(BaseAdvisor 골격).
 *
 * <p>완료 기준 6: Advisor 순서 — 차단이 메모리 저장보다 앞이다. 확인용 실험:
 * {@link Day3AiConfig}에서 이 Advisor의 order를 250(메모리 뒤)으로 바꾸고
 * 인젝션 문장을 보낸 뒤 {@code GET /lab3/chat/history}를 보면, 막았어야 할 문장이
 * 이력에 남아 있다 — 확인 후 반드시 되돌린다.
 */
@Component
public class SafetyAdvisor implements CallAdvisor {

    private static final String REJECTION =
            "보안상 해당 요청은 처리할 수 없습니다. 주문·배송·반품 관련 다른 질문을 해 주세요.";

    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
            Pattern.compile("(?i)(이전|앞선|기존).{0,20}지시.{0,10}무시"),
            Pattern.compile("(?i)ignore.{0,20}(previous|prior|system).{0,20}instruction"),
            Pattern.compile("(?i)(시스템|system).{0,10}(프롬프트|prompt).{0,20}(출력|공개|보여|print|show|reveal)"),
            Pattern.compile("(?<!\\d)\\d{6}-[1-4]\\d{6}(?!\\d)"));

    /**
     * 위험 요청이면 다음 Advisor를 호출하지 않는다. 이 지점에서 체인을 끊어야 사용자 입력이
     * order 200의 메모리 Advisor에 저장되지 않고, 모델 호출 비용도 발생하지 않는다.
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String userText = request.prompt().getUserMessage().getText();
        if (!isBlocked(userText)) {
            return chain.nextCall(request);
        }

        ChatResponse rejection = new ChatResponse(List.of(
                new Generation(new AssistantMessage(REJECTION))));
        HashMap<String, Object> context = new HashMap<>(request.context());
        context.put("safety.blocked", true);
        return new ChatClientResponse(rejection, context);
    }

    private boolean isBlocked(String text) {
        return text != null && BLOCKED_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(text).find());
    }

    @Override
    public String getName() {
        return "safety";
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
