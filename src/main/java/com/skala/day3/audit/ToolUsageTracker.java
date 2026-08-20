package com.skala.day3.audit;

import org.springframework.stereotype.Component;

/**
 * 한 번의 동기 채팅 요청 안에서 도구가 실제 실행됐는지 추적한다.
 * ThreadLocal 값은 요청이 끝날 때 반드시 제거하여 다음 요청으로 상태가 새지 않게 한다.
 */
@Component
public class ToolUsageTracker {

    private final ThreadLocal<State> state = new ThreadLocal<>();

    public void begin(int maxCalls) {
        if (maxCalls < 1) {
            throw new IllegalArgumentException("maxCalls는 1 이상이어야 합니다.");
        }
        state.set(new State(maxCalls));
    }

    /** 도구 실행 직전에 호출하며, 설정된 상한을 넘으면 실제 도구 메서드 진입을 차단한다. */
    public int markUsed() {
        State current = state.get();
        if (current == null) {
            throw new IllegalStateException("도구 호출 추적이 시작되지 않았습니다.");
        }
        if (current.calls >= current.maxCalls) {
            throw new ToolCallLimitExceededException(current.maxCalls);
        }
        current.calls++;
        return current.calls;
    }

    public boolean wasUsed() {
        State current = state.get();
        return current != null && current.calls > 0;
    }

    public int callCount() {
        State current = state.get();
        return current == null ? 0 : current.calls;
    }

    public void clear() {
        state.remove();
    }

    private static final class State {

        private final int maxCalls;
        private int calls;

        private State(int maxCalls) {
            this.maxCalls = maxCalls;
        }
    }
}
