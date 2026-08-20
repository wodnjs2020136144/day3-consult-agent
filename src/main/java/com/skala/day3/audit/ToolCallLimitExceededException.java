package com.skala.day3.audit;

/** 한 번의 상담 요청에서 허용된 도구 호출 횟수를 초과했을 때 발생한다. */
public class ToolCallLimitExceededException extends RuntimeException {

    public ToolCallLimitExceededException(int maxCalls) {
        super("요청당 도구 호출 한도(%d회)를 초과했습니다.".formatted(maxCalls));
    }
}
