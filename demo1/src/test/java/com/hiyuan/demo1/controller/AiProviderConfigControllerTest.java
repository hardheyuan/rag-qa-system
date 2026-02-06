package com.hiyuan.demo1.controller;

import com.hiyuan.demo1.security.JwtAuthenticationFilter;
import com.hiyuan.demo1.service.AiProviderConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AiProviderConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "server.servlet.context-path=/api")
class AiProviderConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiProviderConfigService configService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("AI provider endpoints stay accessible when context path is /api")
    void getSupportedProviders_respectsContextPath() throws Exception {
        given(configService.getSupportedProviders()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/admin/ai-providers/providers").contextPath("/api"))
                .andExpect(status().isOk());
    }
}
