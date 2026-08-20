package com.skala.day3.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.skala.day3.domain.Ticket;
import com.skala.day3.domain.TicketStatus;
import com.skala.day3.repository.TicketRepository;

import static org.mockito.BDDMockito.given;

import java.util.Optional;

/** 관리자 승인 API가 인증과 ADMIN 역할을 모두 요구하는지 검증한다. */
@WebMvcTest(AdminController.class)
@Import(AdminControllerSecurityTest.MethodSecurityConfig.class)
class AdminControllerSecurityTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    TicketRepository tickets;

    @Test
    void 인증하지_않으면_승인할_수_없다() throws Exception {
        mvc.perform(post("/lab3/admin/tickets/T-0001/approve").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void 일반_사용자는_승인할_수_없다() throws Exception {
        mvc.perform(post("/lab3/admin/tickets/T-0001/approve").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void 관리자만_승인할_수_있다() throws Exception {
        given(tickets.approve("T-0001")).willReturn(Optional.of(
                new Ticket("T-0001", "12345", "user1", "단순 변심", TicketStatus.APPROVED)));

        mvc.perform(post("/lab3/admin/tickets/T-0001/approve").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {
    }
}
