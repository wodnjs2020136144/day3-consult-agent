package com.skala.day3.web;

/** 예외 응답에 스택트레이스를 노출하지 않는다 — 안전한 문구 + traceId만. */
public record ErrorResponse(String message, String traceId) {}
