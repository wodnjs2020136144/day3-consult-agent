# day3-consult-agent

Spring AI로 만든 커머스 상담 에이전트 — 주문 조회, 환불 접수, 반품/배송/교환 규정 RAG 답변을
하나의 `ChatClient` 파이프라인으로 처리한다.

SKALA "SpringAI 이해 및 활용" 과정 Day 3 메인 실습(2026-08-20) 결과물이며, 조원 황재원·박성우가
같은 스캐폴드(`c4f5d05`)에서 출발해 각자 브랜치를 완주하고 서로의 브랜치를 교차 공격했다.

## 무엇을 만들었나

- **Tool Calling 주문 조회** — `@Tool` description만으로 모델이 주문 상태 도구를 호출
- **`ToolContext` 권한 격리** — `userId`는 파라미터가 아니라 `ToolContext`로만 전달되어 모델이
  조작할 수 없는 통로로 고정, 남의 주문·ID 주입 시도를 구조적으로 차단
- **승인 게이트** — 환불 도구는 `PENDING` 접수까지만 수행하고, 승인(`approve`)은 도구 목록 밖
  관리자 API 전용
- **RAG 규정 답변** — 반품/배송/교환 규정 문서를 인제스트해 출처(`sources`)와 함께 답변
- **멀티턴 대화 메모리** — 세션 단위 `conversationId`로 대명사 참조·문맥 유지, 세션 간 격리
- **감사 로그 + 계측** — 모든 도구 호출을 가로채 개인정보 마스킹 후 기록, 토큰·지연을
  `/actuator/metrics`로 관찰

## 아키텍처

Advisor는 실행 순서(`order`)가 곧 보안 정책이다 — 차단이 메모리 저장보다 먼저 실행돼야
인젝션 문장이 대화 이력에 남지 않는다.

```
요청 → TokenMeterAdvisor(10) → SafetyAdvisor(100) → MemoryAdvisor(200) → QuestionAnswerAdvisor(300)
     → ChatModel → [OrderTools | RefundTools] → 응답
```

| 패키지 | 역할 |
|---|---|
| `tools/` | 주문 조회·환불 접수 `@Tool` |
| `advisor/` | 인젝션 차단(`SafetyAdvisor`), 토큰/지연 계측(`TokenMeterAdvisor`) |
| `audit/` | 도구 호출 감사 로깅 + 개인정보 마스킹(`ToolAuditAspect`) |
| `rag/` | 규정 문서 인제스트(`PolicyIngestService`) |
| `service/` | `ConsultService` — conversationId 조합, toolUsed 판정 |
| `web/` | REST 엔드포인트, 예외 처리 |

## 기술 스택

Java 21 · Spring Boot 4.1.0 · Spring AI BOM 2.0.0 · Gradle · OpenAI Chat API ·
인메모리 VectorStore(확장 과제로 pgvector 전환 가능, `docker-compose.yml`)

## 빠른 시작

> ⚠️ `main`은 실습 시작 시점의 스캐폴드 상태다(TODO 미구현). 동작하는 코드는 아래 두 작업
> 브랜치에 있다 — 먼저 `git checkout hwangjaewon/day3-consult-agent`(또는
> `parksungwoo/day3-consult-agent`)로 전환한 뒤 실행한다.

```bash
export OPENAI_API_KEY="sk-..."
./gradlew bootRun

curl -X POST localhost:8080/lab3/chat -H 'Content-Type: application/json' \
     -d '{"question":"반품 규정 알려줘","sessionId":"s1"}'
```

Swagger UI — <http://localhost:8080/swagger-ui.html>

## 브랜치 구조와 결과

두 브랜치는 **머지하지 않는다.** Day 3 완료 기준의 핵심은 "만든 사람이 아니라 옆 사람이
공격한다"는 교차 레드팀인데, 코드를 합치면 이 검증 자체가 성립하지 않는다.

| 브랜치 | 담당 | 완료 기준(9개 중) | 교차 레드팀 | 테스트 | 결과보고서 |
|---|---|---|---|---|---|
| [`hwangjaewon/day3-consult-agent`](https://github.com/wodnjs2020136144/day3-consult-agent/tree/hwangjaewon/day3-consult-agent) | 황재원 | 9/9 | 8/8 방어 | 11건 통과 | [결과보고서.md](https://github.com/wodnjs2020136144/day3-consult-agent/blob/hwangjaewon/day3-consult-agent/docs/결과보고서.md) |
| [`parksungwoo/day3-consult-agent`](https://github.com/wodnjs2020136144/day3-consult-agent/tree/parksungwoo/day3-consult-agent) | 박성우 | 9/9 | 최초 3/8 → 보완 후 8/8 | 25건 통과 | [결과보고서.md](https://github.com/wodnjs2020136144/day3-consult-agent/blob/parksungwoo/day3-consult-agent/docs/결과보고서.md) |

## 교차 레드팀에서 나온 것

- **모델이 거절해도 방어가 아니다.** 응답 문구만 정상 거절이어도 `/lab3/chat/history`를 열어보면
  인젝션 문장·개인정보가 대화 메모리에 그대로 저장돼 있던 사례가 있었다 — 도구 호출 여부,
  메모리 저장 결과, HTTP 상태까지 함께 확인해야 실제 방어 여부를 판단할 수 있다.
- **선언만 된 설정값은 없는 것과 같다.** `day3.tool.max-calls=5`가 `application.yml`에 있어도
  실행 코드가 그 값을 읽어 호출을 중단하지 않으면 상한은 존재하지 않는 것과 동일했다.
- **차단은 프롬프트가 아니라 코드로.** 한 번이라도 뚫린 경로는 시스템 프롬프트 문구를 손보는
  대신, Advisor `order` 조정·정규식 확장·`ToolContext` 강제 같은 코드 변경으로 막고 재검증했다.

## 산출물

- [`docs/실습-가이드.md`](docs/실습-가이드.md) — 실습 시작 시점 스캐폴드 안내서(TODO 체크리스트,
  시간 배분, 레드팀 절차 원문)
- 결과보고서 — [황재원 .md](https://github.com/wodnjs2020136144/day3-consult-agent/blob/hwangjaewon/day3-consult-agent/docs/결과보고서.md) ·
  [황재원 .html](https://github.com/wodnjs2020136144/day3-consult-agent/blob/hwangjaewon/day3-consult-agent/docs/결과보고서.html) ·
  [박성우 .md](https://github.com/wodnjs2020136144/day3-consult-agent/blob/parksungwoo/day3-consult-agent/docs/결과보고서.md)
- 레드팀 결과표 — [황재원](https://github.com/wodnjs2020136144/day3-consult-agent/blob/hwangjaewon/day3-consult-agent/docs/레드팀-결과표.md) ·
  [박성우](https://github.com/wodnjs2020136144/day3-consult-agent/blob/parksungwoo/day3-consult-agent/docs/레드팀-결과표.md)
- [트러블슈팅](https://github.com/wodnjs2020136144/day3-consult-agent/blob/hwangjaewon/day3-consult-agent/docs/트러블슈팅.md) ·
  [세션보고서](https://github.com/wodnjs2020136144/day3-consult-agent/tree/hwangjaewon/day3-consult-agent/docs/세션보고서) ·
  [Step별 실행 캡처 16장](https://github.com/wodnjs2020136144/day3-consult-agent/tree/hwangjaewon/day3-consult-agent/docs/캡처)
