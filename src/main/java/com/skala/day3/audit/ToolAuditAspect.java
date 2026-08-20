package com.skala.day3.audit;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
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

    // 주민등록번호(123456-1234567) · 카드번호(1234-1234-1234-1234) · 이메일 형태를 마스킹한다.
    private static final Pattern RRN = Pattern.compile("\\d{6}-\\d{7}");
    private static final Pattern CARD = Pattern.compile("\\d{4}-\\d{4}-\\d{4}-\\d{4}");
    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");

    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object auditToolCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String toolName = joinPoint.getSignature().getName();
        String userId = extractUserId(joinPoint.getArgs());
        String args = mask(Arrays.stream(joinPoint.getArgs())
                .filter(a -> !(a instanceof ToolContext))
                .map(String::valueOf)
                .toList().toString());
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = System.currentTimeMillis() - start;
            audit.info("tool={} user={} args={} status=OK elapsedMs={} result={}",
                    toolName, userId, args, elapsedMs, mask(String.valueOf(result)));
            return result;
        } catch (Throwable e) {
            long elapsedMs = System.currentTimeMillis() - start;
            audit.warn("tool={} user={} args={} status=FAIL elapsedMs={} error={}",
                    toolName, userId, args, elapsedMs, e.getMessage());
            throw e;
        }
    }

    private String extractUserId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof ToolContext ctx) {
                Object userId = ctx.getContext().get("userId");
                return userId != null ? String.valueOf(userId) : "unknown";
            }
        }
        return "unknown";
    }

    private String mask(String text) {
        String masked = RRN.matcher(text).replaceAll("******-*******");
        masked = CARD.matcher(masked).replaceAll("****-****-****-****");
        masked = EMAIL.matcher(masked).replaceAll("***@***");
        return masked;
    }
}
