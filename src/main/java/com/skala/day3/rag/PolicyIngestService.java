package com.skala.day3.rag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

/**
 * 기동 시 {@code resources/day3-docs/*.md}를 읽어 청크로 나누고 벡터스토어에 넣는다.
 * 완성 상태로 제공된다 — 손대지 않는다({@code ch11_advisors/IngestService.java} 참조).
 *
 * <p>인메모리 VectorStore라 재시작마다 다시 인제스트한다 — 문서 단위 삭제(delete) 로직은
 * 필요 없다(운영 pgvector로 갈 때는 {@code ch07_rag/IngestService.java}의 재색인 패턴을 참고).
 */
@Service
public class PolicyIngestService {

    private static final Logger log = LoggerFactory.getLogger(PolicyIngestService.class);

    private final VectorStore vectorStore;

    public PolicyIngestService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 인제스트는 임베딩 모델을 호출한다 — {@code OPENAI_API_KEY}가 없거나 잘못됐으면 여기서 실패한다.
     * 이 실패가 앱 전체 기동을 막으면 안 된다(CLAUDE.md: AI 호출 실패가 전체 응답 실패로 번지지
     * 않게 폴백을 둔다 — 기동 시점에도 같은 원칙을 적용한다). 실패해도 앱은 뜨고, RAG 답변만
     * 근거 없이 나간다 — 키를 맞춘 뒤 재시작하면 정상화된다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void ingestOnStartup() {
        try {
            Resource[] files = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:day3-docs/*.md");

            List<Document> chunks = Stream.of(files)
                    .flatMap(file -> {
                        List<Document> raw = new TextReader(file).get();
                        List<Document> split = TokenTextSplitter.builder()
                                .withChunkSize(400)
                                .withMinChunkSizeChars(200)
                                .build()
                                .apply(raw);
                        return split.stream().map(c -> enrich(c, file.getFilename()));
                    })
                    .toList();

            vectorStore.add(chunks);
            log.info("[RAG] 규정 문서 {}건 → 청크 {}건 인제스트 완료", files.length, chunks.size());
        } catch (Exception e) {
            log.error("[RAG] 규정 문서 인제스트 실패 — OPENAI_API_KEY를 확인하세요. RAG 답변은 근거 없이 나갑니다. {}",
                    e.getMessage());
        }
    }

    private Document enrich(Document chunk, String filename) {
        Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());
        metadata.put("source", filename);
        metadata.put("version", "2026-08");
        return new Document(chunk.getText(), metadata);
    }
}
