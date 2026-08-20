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

    // TODO ⑥: builder.defaultAdvisors(...)에 아래 순서로 조립한다.
    //   order  10  tokenMeter        — 계측은 가장 바깥(전체 시간을 잰다)
    //   order 100  safety            — 차단은 저장보다 앞
    //   order 200  MessageChatMemoryAdvisor.builder(chatMemory).order(200).build()
    //   order 300  QuestionAnswerAdvisor.builder(vectorStore)
    //                  .searchRequest(SearchRequest.builder()
    //                      .topK(props.rag().topK())
    //                      .similarityThreshold(props.rag().threshold())
    //                      .build())
    //                  .order(300).build()
    // TODO ⑥: builder.defaultTools(orderTools, refundTools)로 도구 두 개를 등록한다.
    @Bean
    public ChatClient assistantChatClient(ChatClient.Builder builder,
                                          VectorStore vectorStore,
                                          ChatMemory chatMemory,
                                          Day3Properties props,
                                          SafetyAdvisor safety,
                                          TokenMeterAdvisor tokenMeter,
                                          OrderTools orderTools,
                                          RefundTools refundTools) {
        throw new UnsupportedOperationException("TODO ⑥: Day3AiConfig.assistantChatClient 를 구현하세요");
    }
}
