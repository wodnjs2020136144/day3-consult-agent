package com.skala.day3.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
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

    // TODO ④: 차단할 패턴(예: "이전 지시 무시", "시스템 프롬프트 출력", 주민등록번호 형태)을
    //          정의하고, before()에서 사용자 메시지에 매칭되면 체인을 계속 진행하지 않고
    //          바로 거절 응답을 만들어 반환한다(가장 단순하게는 요청에 안전 지시를 덧붙이거나,
    //          AdvisorChain을 호출하지 않고 직접 ChatClientResponse를 구성해 반환한다).
    @Override
    public ChatClientRequest before(ChatClientRequest req, AdvisorChain chain) {
        throw new UnsupportedOperationException("TODO ④: SafetyAdvisor.before 를 구현하세요");
    }

    @Override
    public ChatClientResponse after(ChatClientResponse res, AdvisorChain chain) {
        return res;
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
