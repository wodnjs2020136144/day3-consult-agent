package com.skala.day3.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.skala.day3.advisor.SafetyAdvisor;
import com.skala.day3.advisor.TokenMeterAdvisor;
import com.skala.day3.tools.OrderTools;
import com.skala.day3.tools.RefundTools;

/**
 * Step 4 — ChatClient 조립(교안 p.301), 완료 기준 4·6.
 *
 * <p>목적: 공통으로 해야 할 일은 Advisor로 모은다. <b>순서가 곧 정책이다</b> — 차단은
 * 저장(메모리)보다 앞에 있어야 한다. 도구는 {@code defaultTools}로 붙인다.
 *
 * <p>참고: {@code SpringAI_실습/ch11_advisors/MemoryChatConfig.java},
 * {@code SpringAI_실습/12_Advisor순서/Lab12Config.java}
 *
 * <p>완료 기준:
 * <ul>
 *   <li>4. RAG 결합 — 규정 답변에 출처가 붙는다({@link QuestionAnswerAdvisor})</li>
 *   <li>6. Advisor 순서 — 차단(order 100)이 메모리 저장(order 200)보다 앞</li>
 * </ul>
 */
@Configuration
public class Day3AiConfig {

    // Advisor 순서: 계측(10) → 차단(100) → 메모리(200) → RAG(300).
    // 모델이 사용할 수 있는 도구는 주문 조회와 환불 접수 두 개로 제한한다.
    @Bean
    public ChatClient assistantChatClient(ChatClient.Builder builder,
                                          VectorStore vectorStore,
                                          ChatMemory chatMemory,
                                          Day3Properties props,
                                          SafetyAdvisor safety,
                                          TokenMeterAdvisor tokenMeter,
                                          OrderTools orderTools,
                                          RefundTools refundTools) {
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .order(200)
                .build();

        SearchRequest searchRequest = SearchRequest.builder()
                .topK(props.rag().topK())
                .similarityThreshold(props.rag().threshold())
                .build();
        QuestionAnswerAdvisor ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .order(300)
                .build();

        // 계측은 order 10으로 가장 바깥에 두어 Safety·Memory·RAG·모델까지 전체 시간을 잰다.
        return builder
                .defaultAdvisors(tokenMeter, safety, memoryAdvisor, ragAdvisor)
                .defaultTools(orderTools, refundTools)
                .build();
    }
}
