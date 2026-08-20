package com.skala.day3.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import com.skala.day3.repository.OrderRepository;
import com.skala.day3.repository.TicketRepository;
import com.skala.day3.tools.RefundTools;

/** Step 3 감사 로그가 호출 정보는 남기고 개인정보는 노출하지 않는지 확인한다. */
@ExtendWith({SpringExtension.class, OutputCaptureExtension.class})
@ContextConfiguration(classes = ToolAuditAspectTest.TestConfig.class)
class ToolAuditAspectTest {

    @Autowired
    RefundTools refundTools;

    @Autowired
    MeterRegistry registry;

    @Test
    void 도구_호출을_기록하고_개인정보를_마스킹한다(CapturedOutput output) throws Throwable {
        String result = refundTools.requestRefund(
                "12345",
                "연락처 user@example.com, 카드 1111-2222-3333-4444",
                new ToolContext(Map.of("userId", "user1")));

        assertThat(result).contains("접수");
        assertThat(output)
                .contains("tool=RefundTools#requestRefund")
                .contains("user=user1")
                .contains("status=OK")
                .contains("***@***")
                .contains("****-****-****-****")
                .doesNotContain("user@example.com")
                .doesNotContain("1111-2222-3333-4444");
        assertThat(registry.get("ai.tool.calls")
                .tags("tool", "RefundTools#requestRefund", "result", "ok")
                .counter()
                .count()).isEqualTo(1.0);
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        OrderRepository orderRepository() {
            return new OrderRepository();
        }

        @Bean
        TicketRepository ticketRepository() {
            return new TicketRepository();
        }

        @Bean
        RefundTools refundTools(OrderRepository orders, TicketRepository tickets) {
            return new RefundTools(orders, tickets);
        }

        @Bean
        ToolUsageTracker toolUsageTracker() {
            return new ToolUsageTracker();
        }

        @Bean
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        ToolAuditAspect toolAuditAspect(ToolUsageTracker usageTracker, MeterRegistry registry) {
            return new ToolAuditAspect(usageTracker, registry);
        }
    }
}
