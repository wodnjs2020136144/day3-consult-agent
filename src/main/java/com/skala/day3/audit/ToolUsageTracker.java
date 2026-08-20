package com.skala.day3.audit;

import org.springframework.stereotype.Component;

/**
 * 한 번의 동기 채팅 요청 안에서 도구가 실제 실행됐는지 추적한다.
 * ThreadLocal 값은 요청이 끝날 때 반드시 제거하여 다음 요청으로 상태가 새지 않게 한다.
 */
@Component
public class ToolUsageTracker {

    private final ThreadLocal<Boolean> used = ThreadLocal.withInitial(() -> false);

    public void begin() {
        used.set(false);
    }

    public void markUsed() {
        used.set(true);
    }

    public boolean wasUsed() {
        return used.get();
    }

    public void clear() {
        used.remove();
    }
}
