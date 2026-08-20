package com.skala.day3.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 실습에서 조정할 값을 코드가 아니라 설정으로 뺀다 — {@code application.yml}의 {@code day3.*} 블록.
 * 완성 상태로 제공된다 — 손대지 않는다.
 *
 * @param rag   RAG 검색 파라미터
 * @param memory 대화 메모리 윈도우
 * @param tool  도구 호출 안전장치(Step 3 함정 "같은 도구를 무한 호출" 대비 상한)
 */
@ConfigurationProperties(prefix = "day3")
public record Day3Properties(Rag rag, Memory memory, Tool tool) {

    public record Rag(int topK, double threshold) {}

    public record Memory(int max) {}

    public record Tool(int maxCalls) {}
}
