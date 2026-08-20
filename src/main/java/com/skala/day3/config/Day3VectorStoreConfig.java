package com.skala.day3.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 추가 인프라 없이 바로 실행할 수 있도록 인메모리 VectorStore 를 기본으로 둔다.
 *
 * <p><b>운영에서는 쓰지 않는다.</b> 재시작하면 사라지고, 인스턴스마다 따로 갖는다.
 * pgvector 스타터를 추가하면 이 빈은 자동으로 물러난다({@code @ConditionalOnMissingBean}) —
 * 확장 과제 "pgvector 전환"을 고르면 {@code docker-compose.yml}과 함께 그대로 쓸 수 있다.
 *
 * <p>완성 상태로 제공된다 — 손대지 않는다({@code ch07_rag/VectorStoreConfig.java} 패턴).
 */
@Configuration
public class Day3VectorStoreConfig {

    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
