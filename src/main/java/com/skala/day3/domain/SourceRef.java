package com.skala.day3.domain;

/** RAG 답변에 붙이는 출처 — 문서명과 버전. */
public record SourceRef(String document, String version) {}
