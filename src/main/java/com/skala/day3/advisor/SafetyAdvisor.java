package com.skala.day3.advisor;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Component;

/**
 * ★ TODO ④ — Step 4 (교안 p.301), 완료 기준 6.
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
public class SafetyAdvisor implements BaseAdvisor {

    private static final String REJECTION_MESSAGE =
            "죄송하지만 해당 요청은 처리할 수 없습니다. 상담 가능한 범위 내에서 다시 문의해 주세요.";

    // 인젝션(지시 무시·시스템 프롬프트 노출) · 관리자 사칭 · 주민등록번호 형태 · 과도하게 긴 입력(비용 공격)을 막는다.
    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
            Pattern.compile("이전\\s*지시.*무시"),
            Pattern.compile("모든\\s*지시.*무시"),
            Pattern.compile("시스템\\s*프롬프트"),
            Pattern.compile("(너의|당신의)\\s*(규칙|프롬프트|지시사항)"),
            Pattern.compile("나\\s*관리자"),
            Pattern.compile("관리자\\s*권한"),
            Pattern.compile("\\d{6}-\\d{7}"));

    private static final int MAX_INPUT_LENGTH = 2000;

    // before()/after()는 BaseAdvisor 인터페이스 요구사항이라 구현하되, 실제 차단 로직은
    // adviseCall()에서 처리한다 — before()만으로는 체인 진행 자체를 막을 수 없기 때문이다.
    @Override
    public ChatClientRequest before(ChatClientRequest req, AdvisorChain chain) {
        return req;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse res, AdvisorChain chain) {
        return res;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        String userText = request.prompt().getUserMessage().getText();
        if (isBlocked(userText)) {
            return ChatClientResponse.builder()
                    .chatResponse(new ChatResponse(List.of(new Generation(new AssistantMessage(REJECTION_MESSAGE)))))
                    .context(request.context())
                    .build();
        }
        return chain.nextCall(request);
    }

    private boolean isBlocked(String text) {
        if (text == null) {
            return false;
        }
        if (text.length() > MAX_INPUT_LENGTH) {
            return true;
        }
        return BLOCKED_PATTERNS.stream().anyMatch(p -> p.matcher(text).find());
    }

    @Override
    public String getName() {
        return "safety";
    }

    // TODO ④: getOrder()가 메모리 Advisor(order 200)보다 작아야 한다(예: 100).
    @Override
    public int getOrder() {
        return 100;
    }
}
