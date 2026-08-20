package com.skala.day3.domain;

import java.time.LocalDate;

/**
 * 데모용 고정 주문 데이터. 실제로는 리포지토리가 DB를 조회한다.
 *
 * @param id      주문번호
 * @param ownerId 소유자 — 이 값이 곧 권한 격리의 기준이다({@link OrderTools} TODO 참고)
 */
public record Order(String id, String ownerId, String item, OrderStatus status, LocalDate eta) {}
