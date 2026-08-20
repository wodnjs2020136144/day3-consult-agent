package com.skala.day3.audit;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Step 3 — 도구 감사 로그(교안 p.300), 완료 기준 7.
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
 * 이원화 문제는 {@link com.skala.day3.advisor} 패키지의 Advisor에서 다룬다.
 */
@Aspect
@Component
public class ToolAuditAspect {

    private static final Logger audit = LoggerFactory.getLogger("AI_TOOL_AUDIT");
    private final ToolUsageTracker usageTracker;
    private final MeterRegistry registry;

    public ToolAuditAspect(ToolUsageTracker usageTracker, MeterRegistry registry) {
        this.usageTracker = usageTracker;
        this.registry = registry;
    }

    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object auditToolCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String tool = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "#" + joinPoint.getSignature().getName();
        Object[] rawArgs = joinPoint.getArgs();
        String userId = currentUser(rawArgs);
        String args = mask(Arrays.toString(rawArgs));
        long started = System.nanoTime();

        try {
            // 실제 도구 진입 전에 횟수를 검사하여 6번째 호출부터 업무 로직을 실행하지 않는다.
            int callNumber = usageTracker.markUsed();
            Object result = joinPoint.proceed();
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;
            registry.counter("ai.tool.calls", "tool", tool, "result", "ok").increment();
            audit.info("tool={} call={} user={} args={} result={} status=OK elapsedMs={}",
                    tool, callNumber, userId, args, mask(String.valueOf(result)), elapsedMs);
            return result;
        } catch (Throwable e) {
            long elapsedMs = (System.nanoTime() - started) / 1_000_000;
            registry.counter("ai.tool.calls", "tool", tool, "result", "fail").increment();
            audit.error("tool={} user={} args={} status=FAIL error={} elapsedMs={}",
                    tool, userId, args, mask(e.toString()), elapsedMs);
            throw e;
        }
    }

    /** ToolContext는 모델이 수정할 수 없는 사용자 식별 경로이므로 별도 감사 필드로 기록한다. */
    private String currentUser(Object[] args) {
        return Arrays.stream(args)
                .filter(ToolContext.class::isInstance)
                .map(ToolContext.class::cast)
                .map(ToolContext::getContext)
                .map(context -> context.get("userId"))
                .filter(java.util.Objects::nonNull)
                .map(Object::toString)
                .findFirst()
                .map(this::mask)
                .orElse("unknown");
    }

    /** 실습용 마스킹 규칙. 운영에서는 도메인별 규칙과 로그 보존 기간을 함께 관리한다. */
    private String mask(String raw) {
        return raw
                .replaceAll("\\d{6}-\\d{7}", "******-*******")
                .replaceAll("\\d{4}-\\d{4}-\\d{4}-\\d{4}", "****-****-****-****")
                .replaceAll("[\\w.+-]+@[\\w-]+\\.[\\w.]+", "***@***");
    }
}
