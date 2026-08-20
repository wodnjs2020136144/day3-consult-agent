package com.skala.day3.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 대화 메모리 배선. 완성 상태로 제공된다 — 손대지 않는다.
 *
 * <p>개발·단일 인스턴스에서는 인메모리로 충분하다. 운영에서 인스턴스가 두 대가 되는
 * 순간 대화가 왔다 갔다 하므로 JDBC·Redis 리포지토리로 바꿔야 한다.
 *
 * <p>길이 상한은 {@link Day3Properties#memory()}의 {@code max}에서 온다 — 코드에 상수를 남기지 않는다.
 */
@Configuration
public class Day3ChatMemoryConfig {

    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository repository, Day3Properties props) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(props.memory().max())
                .build();
    }
}
