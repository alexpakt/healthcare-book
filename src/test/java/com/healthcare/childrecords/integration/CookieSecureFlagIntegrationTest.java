package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.LoginRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The test profile disables the Secure flag. This class runs with it enabled, as production does,
 * so the flag is checked where it matters.
 */
@TestPropertySource(properties = "app.auth.cookie.secure=true")
public class CookieSecureFlagIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testLogin_CookieIsSecureWhenConfigured() throws Exception {
    String username = "securecookie" + System.nanoTime();
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new RegisterParentRequest(
                    username, "TestPass123!", "Secure", "Parent", username + "@example.com", null, null))))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new LoginRequest(username, "TestPass123!"))))
        .andExpect(status().isOk())
        .andExpect(cookie().httpOnly("access_token", true))
        .andExpect(cookie().secure("access_token", true));
  }
}
