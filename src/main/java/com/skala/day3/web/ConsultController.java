package com.skala.day3.web;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.skala.day3.domain.ChatAnswer;
import com.skala.day3.domain.ChatRequest;
import com.skala.day3.service.ConsultService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * 상담 API. 완성 상태로 제공된다 — 손대지 않는다.
 *
 * <p>사용자 ID는 요청 파라미터를 신뢰하지 않고 Spring Security 인증 주체에서만 꺼낸다.
 */
@RestController
@Tag(name = "Day3 메인 실습 · 상담 에이전트")
@SecurityRequirement(name = "basicAuth")
public class ConsultController {

    private final ConsultService consult;

    public ConsultController(ConsultService consult) {
        this.consult = consult;
    }

    @PostMapping("/lab3/chat")
    @Operation(summary = "상담 채팅", description = "RAG 규정 답변 + 주문 조회/환불 도구 + 대화 메모리")
    public ChatAnswer chat(@Valid @RequestBody ChatRequest request, Principal principal) {
        return consult.ask(request.question(), principal.getName(), request.sessionId());
    }

    @GetMapping("/lab3/chat/history")
    @Operation(summary = "대화 이력 조회")
    public List<String> history(String sessionId, Principal principal) {
        return consult.history(principal.getName(), sessionId);
    }

    @DeleteMapping("/lab3/chat/history")
    @Operation(summary = "대화 이력 초기화", description = "Advisor 순서 실험 후 원복할 때 사용한다.")
    public void clearHistory(String sessionId, Principal principal) {
        consult.clearHistory(principal.getName(), sessionId);
    }
}
