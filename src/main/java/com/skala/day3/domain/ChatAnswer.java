package com.skala.day3.domain;

import java.util.List;

/** 화면이 쓰기 좋게 답변·출처·도구사용 여부를 나눠 반환한다(교안 Phase 6, p.320). */
public record ChatAnswer(String answer, List<SourceRef> sources, boolean toolUsed) {}
