package com.skala.day3.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ★ TODO ③ — Step 3 (교안 p.300), 완료 기준 7.
 *
 * <p>목적: {@code @Tool}이 붙은 모든 메서드 호출을 한 곳에서 감사 로깅한다. 도구마다
 * 로깅 코드를 넣으면 반드시 빠뜨리는 곳이 생긴다. 인자에 개인정보가 있으면 마스킹한다.
 *
 * <p>참고: {@code SpringAI_실습/ch10_toolsafe/ToolAuditAspect.java},
 * {@code SpringAI_실습/11_승인게이트/ToolAudit.java}
 *
 * <p>완료 기준 7: 모든 도구 호출을 추적할 수 있다 — 도구명·인자·사용자·결과가 로그에 남는다.
 *
 * <p>⚠️ 스트리밍에서 감사가 누락되는 흔한 실수(교안 트러블슈팅 표)는 이 Aspect가
 * 메서드 호출 자체를 가로채므로 해당하지 않는다 — {@code CallAdvisor}/{@code StreamAdvisor}
 * 이원화 문제는 {@link com.skala.day3.advisor} 패키지 쪽 TODO에서 다룬다.
 */
@Aspect
@Component
public class ToolAuditAspect {

    private static final Logger audit = LoggerFactory.getLogger("AI_TOOL_AUDIT");

    // TODO ③: @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")로
    //          모든 @Tool 메서드 호출을 가로챈다.
    // TODO ③: 도구명(joinPoint.getSignature()...) · 인자(joinPoint.getArgs()) · 결과를 로깅한다.
    //          인자에 주민등록번호·카드번호·이메일 형태가 있으면 마스킹한다(정규식 치환).
    // TODO ③: 예외가 나면 status=FAIL로 기록하고 다시 던진다(대화 전체를 막지 않는다).
    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object auditToolCall(ProceedingJoinPoint joinPoint) throws Throwable {
        throw new UnsupportedOperationException("TODO ③: ToolAuditAspect.auditToolCall 을 구현하세요");
    }
}
