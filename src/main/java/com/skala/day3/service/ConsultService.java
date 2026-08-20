package com.skala.day3.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import com.skala.day3.domain.ChatAnswer;
import com.skala.day3.domain.SourceRef;

/**
 * ★ TODO ⑦ — Step 5 (교안 p.302), 완료 기준 5.
 *
 * <p>목적: {@code conversationId}를 <b>이 한 곳에서만</b> 만든다. 규칙이 흩어지면 남의 대화가
 * 섞이는 사고가 난다 — 메모리에서 가장 흔한 버그이고, 가장 늦게 발견된다(교안 함정).
 * 사용자 ID는 {@code toolContext}로 넘겨 도구가 권한 검증에 쓸 수 있게 한다.
 *
 * <p>참고: 교안 Phase 3(p.316), Phase 5(p.319) 코드.
 *
 * <p>완료 기준 5: 멀티턴 — 대명사 후속 질문("그럼 그거 반품 돼요?")이 이전 턴을 참조해 응답한다.
 * 검증: 새 세션에서 "그거 어떻게 됐어요?"를 물으면 맥락이 없어 되묻는다(세션 격리).
 */
@Service
public class ConsultService {

    private final ChatClient chat;
    private final ChatMemory chatMemory;

    public ConsultService(ChatClient assistantChatClient, ChatMemory chatMemory) {
        this.chat = assistantChatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * 대화 ID 규칙 — 사용자·세션을 합쳐 하나로 만든다. 이 메서드 밖에서 조합하지 않는다.
     */
    public String conversationId(String userId, String sessionId) {
        return "%s:%s".formatted(userId, sessionId);
    }

    public ChatAnswer ask(String question, String userId, String sessionId) {
        AtomicBoolean toolUsed = new AtomicBoolean(false);

        ChatClientResponse response = chat.prompt().user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId(userId, sessionId)))
                .toolContext(Map.of("userId", userId, "toolUsed", toolUsed))
                .call().chatClientResponse();

        String answer = response.chatResponse().getResult().getOutput().getText();
        return new ChatAnswer(answer, sourcesFrom(response), toolUsed.get());
    }

    public List<String> history(String userId, String sessionId) {
        return chatMemory.get(conversationId(userId, sessionId)).stream()
                .map(m -> m.getMessageType() + ": " + m.getText())
                .toList();
    }

    public void clearHistory(String userId, String sessionId) {
        chatMemory.clear(conversationId(userId, sessionId));
    }

    /** 참고용 — QuestionAnswerAdvisor의 컨텍스트 키에서 출처를 뽑는 헬퍼(TODO ⑦에서 활용). */
    @SuppressWarnings("unchecked")
    protected List<SourceRef> sourcesFrom(ChatClientResponse response) {
        Object raw = response.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        if (!(raw instanceof List<?> docs)) {
            return List.of();
        }
        return ((List<Document>) docs).stream()
                .map(d -> new SourceRef(
                        String.valueOf(d.getMetadata().get("source")),
                        String.valueOf(d.getMetadata().get("version"))))
                .distinct()
                .toList();
    }
}
