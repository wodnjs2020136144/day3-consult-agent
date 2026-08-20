package com.skala.day3.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param question  사용자 질문
 * @param sessionId 프런트가 발급하는 세션 식별자 — 사용자·세션을 합쳐 conversationId 를 만든다
 */
public record ChatRequest(
        @NotBlank(message = "질문은 비어 있을 수 없습니다.")
        @Size(max = 2000, message = "질문은 2,000자 이하여야 합니다.")
        String question,

        @NotBlank(message = "sessionId는 비어 있을 수 없습니다.")
        @Size(max = 100, message = "sessionId는 100자 이하여야 합니다.")
        String sessionId) {}
