package com.healthcare.childrecords.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.childrecords.dto.request.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

  private static final java.util.concurrent.atomic.AtomicLong AMKA_SEQUENCE =
      new java.util.concurrent.atomic.AtomicLong(System.currentTimeMillis() % 1_000_000_000L);

  protected static String uniqueAmka() {
    return String.format("%011d", AMKA_SEQUENCE.incrementAndGet());
  }

  @Autowired protected MockMvc mockMvc;

  @Autowired protected ObjectMapper objectMapper;

  protected String loginAndGetToken(String username, String password) throws Exception {
    LoginRequest loginRequest = new LoginRequest(username, password);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

    return result.getResponse().getCookie("access_token").getValue();
  }

  protected String getAuthorizationHeader(String token) {
    return "Bearer " + token;
  }
}
