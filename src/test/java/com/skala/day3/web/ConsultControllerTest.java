package com.skala.day3.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import com.skala.day3.domain.ChatAnswer;
import com.skala.day3.service.ConsultService;

/**
 * 웹 계층만 확인 — 모델을 부르지 않으므로 키 없이 돈다.
 * {@code ConsultService}를 Mockito로 대체해 컨트롤러 배선만 검증한다(완성 상태 — 손대지 않는다).
 *
 * <p>TODO ⑦(ConsultService)이 완성되기 전에도 이 테스트는 통과한다 — 서비스는 목이기 때문이다.
 */
@WebMvcTest(ConsultController.class)
class ConsultControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ConsultService consultService;

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void 채팅_요청은_200과_구조화된_답변을_돌려준다() throws Exception {
        given(consultService.ask("반품 규정 알려줘", "user1", "s1"))
                .willReturn(new ChatAnswer("7일 이내 반품 가능합니다.", List.of(), false));

        mvc.perform(post("/lab3/chat")
                        .param("userId", "user2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"반품 규정 알려줘","sessionId":"s1"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("7일 이내 반품 가능합니다."));
        verify(consultService).ask("반품 규정 알려줘", "user1", "s1");
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void 이력_조회는_200을_돌려준다() throws Exception {
        given(consultService.history("user1", "s1")).willReturn(List.of("USER: 안녕"));

        mvc.perform(get("/lab3/chat/history").param("sessionId", "s1").param("userId", "user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("USER: 안녕"));
        verify(consultService).history("user1", "s1");
    }

    @Test
    void 인증하지_않으면_상담_API를_호출할_수_없다() throws Exception {
        mvc.perform(post("/lab3/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"반품 규정 알려줘","sessionId":"s1"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void 이천자를_넘는_질문은_모델_호출_전에_거절한다() throws Exception {
        mvc.perform(post("/lab3/chat")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"question":"%s","sessionId":"s1"}""".formatted("A".repeat(2001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("질문은 2,000자 이하여야 합니다."));
    }
}
