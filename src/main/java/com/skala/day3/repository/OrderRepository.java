package com.skala.day3.repository;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.skala.day3.domain.Order;
import com.skala.day3.domain.OrderStatus;

/**
 * 데모용 인메모리 주문 저장소. 완성 상태로 제공된다 — 손대지 않는다.
 *
 * <p><b>소유자 조건이 쿼리 안에 있다.</b> {@link #findByIdAndOwnerId}는 존재하지 않는 주문과
 * 남의 주문을 구분하지 않고 똑같이 빈 값을 반환한다 — 그 자체가 정보 노출이기 때문이다
 * (교안 p.298·p.305). TODO에서 도구를 만들 때 이 메서드만 쓰면 소유자 검증을 빠뜨릴 수 없다.
 */
@Repository
public class OrderRepository {

    private static final Map<String, Order> ORDERS = Map.of(
            "12345", new Order("12345", "user1", "무선 이어폰", OrderStatus.배송중, LocalDate.of(2026, 8, 25)),
            "12346", new Order("12346", "user1", "USB-C 케이블", OrderStatus.배송완료, LocalDate.of(2026, 8, 10)),
            "99999", new Order("99999", "user2", "노트북 스탠드", OrderStatus.결제완료, LocalDate.of(2026, 8, 28)));

    public Optional<Order> findByIdAndOwnerId(String orderId, String ownerId) {
        return Optional.ofNullable(ORDERS.get(orderId))
                .filter(o -> o.ownerId().equals(ownerId));
    }
}
