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
 * ★ TODO ⑥ — Step 4 (교안 p.301), 완료 기준 4·6.
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

    @Bean
    public ChatClient assistantChatClient(ChatClient.Builder builder,
                                          VectorStore vectorStore,
                                          ChatMemory chatMemory,
                                          Day3Properties props,
                                          SafetyAdvisor safety,
                                          TokenMeterAdvisor tokenMeter,
                                          OrderTools orderTools,
                                          RefundTools refundTools) {
        return builder
                .defaultSystem("""
                        당신은 쇼핑몰 상담 에이전트입니다. 사용자의 주문 조회·환불 접수·배송/반품/교환
                        규정 안내를 돕습니다. 본인 소유가 아닌 주문 정보는 절대 알려주지 않습니다.
                        규정에 없는 내용을 지어내지 말고, 확실하지 않으면 모른다고 답하세요.
                        """)
                .defaultAdvisors(
                        tokenMeter,
                        safety,
                        MessageChatMemoryAdvisor.builder(chatMemory).order(200).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(props.rag().topK())
                                        .similarityThreshold(props.rag().threshold())
                                        .build())
                                .order(300)
                                .build())
                .defaultTools(orderTools, refundTools)
                .build();
    }
}
