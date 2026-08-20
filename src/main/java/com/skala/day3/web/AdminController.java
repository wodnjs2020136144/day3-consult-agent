package com.skala.day3.web;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.skala.day3.domain.Ticket;
import com.skala.day3.repository.TicketRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 실제 처리(승인)는 사람이 누른다. 완성 상태로 제공된다 — 손대지 않는다.
 *
 * <p><b>이 경로는 도구 목록에 없다.</b> 그래서 모델은 아무리 지시받아도 이 엔드포인트를
 * 부를 수 없다 — "승인까지 네가 해줘" 같은 레드팀 공격(Step 7)이 막히는 이유가 이것이다.
 *
 * <p>실무에서는 {@code @PreAuthorize("hasRole('ADMIN')")}로 인가를 강제한다
 * (Spring Security 확장 과제 — {@code SpringAI_실습/ch10_toolsafe} 참고).
 */
@RestController
@Tag(name = "Day3 메인 실습 · 관리자(승인)")
@SecurityRequirement(name = "basicAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TicketRepository tickets;

    public AdminController(TicketRepository tickets) {
        this.tickets = tickets;
    }

    @GetMapping("/lab3/admin/tickets/pending")
    @Operation(summary = "승인 대기 티켓 목록")
    public List<Ticket> pending() {
        return tickets.pending();
    }

    @PostMapping("/lab3/admin/tickets/{no}/approve")
    @Operation(summary = "사람이 누르는 승인", description = "도구 목록에 없으므로 모델은 이 경로에 닿을 수 없다.")
    public Map<String, Object> approve(@PathVariable String no) {
        return tickets.approve(no)
                .<Map<String, Object>>map(t -> Map.of("no", t.no(), "status", t.status().name()))
                .orElse(Map.of("no", no, "status", "NOT_FOUND"));
    }
}
