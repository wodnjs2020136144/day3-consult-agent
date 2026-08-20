package com.skala.day3.domain;

/**
 * @param question  사용자 질문
 * @param sessionId 프런트가 발급하는 세션 식별자 — 사용자·세션을 합쳐 conversationId 를 만든다
 */
public record ChatRequest(String question, String sessionId) {}
