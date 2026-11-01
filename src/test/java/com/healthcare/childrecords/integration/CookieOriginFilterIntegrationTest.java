package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.request.LoginRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.entity.Gender;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Two layers protect cookie-authenticated requests:
 * <ul>
 *   <li>Spring's CORS filter rejects any request whose Origin is not in app.cors.allowed-origins
 *       ("Invalid CORS request"), before authentication runs.</li>
 *   <li>CookieOriginFilter rejects state-changing requests that carry the auth cookie and no
 *       Authorization header, unless their Origin is allowed. A request without an Origin header
 *       is rejected with "Origin not allowed". Only this filter produces that message.</li>
 * </ul>
 * The test profile allows http://localhost:5173 only.
 */
public class CookieOriginFilterIntegrationTest extends BaseIntegrationTest {

  private static final String ALLOWED_ORIGIN = "http://localhost:5173";
  private static final String FOREIGN_ORIGIN = "https://evil.example";

  private Cookie registerParentAndGetCookie() throws Exception {
    String username = "originparent" + System.nanoTime();
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new RegisterParentRequest(
                    username, "TestPass123!", "Origin", "Parent", username + "@example.com", null, null))))
        .andExpect(status().isCreated());
    return mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new LoginRequest(username, "TestPass123!"))))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getCookie("access_token");
  }

  private String childBody() throws Exception {
    return objectMapper.writeValueAsString(
        new ChildRequest("Origin", "Child", uniqueAmka(), "2020-01-15", Gender.MALE, null));
  }

  @Test
  public void testCookiePost_FromForeignOrigin_IsRejectedByCorsLayer() throws Exception {
    Cookie cookie = registerParentAndGetCookie();

    mockMvc
        .perform(
            post("/api/children")
                .cookie(cookie)
                .header("Origin", FOREIGN_ORIGIN)
                .contentType("application/json")
                .content(childBody()))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testCookiePost_WithoutOrigin_IsRejectedByCookieOriginFilter() throws Exception {
    Cookie cookie = registerParentAndGetCookie();

    var result = mockMvc
        .perform(
            post("/api/children")
                .cookie(cookie)
                .contentType("application/json")
                .content(childBody()))
        .andExpect(status().isForbidden())
        .andReturn();

    assertEquals("Origin not allowed", result.getResponse().getContentAsString());
  }

  @Test
  public void testCookiePost_FromAllowedOrigin_IsAccepted() throws Exception {
    Cookie cookie = registerParentAndGetCookie();

    mockMvc
        .perform(
            post("/api/children")
                .cookie(cookie)
                .header("Origin", ALLOWED_ORIGIN)
                .contentType("application/json")
                .content(childBody()))
        .andExpect(status().isCreated());
  }

  @Test
  public void testCookieGet_WithoutOrigin_IsAllowed() throws Exception {
    // Safe methods change no state, so CookieOriginFilter does not check them.
    Cookie cookie = registerParentAndGetCookie();

    mockMvc
        .perform(get("/api/auth/me").cookie(cookie))
        .andExpect(status().isOk());
  }

  @Test
  public void testBearerPost_WithoutOrigin_IsNotCheckedByCookieOriginFilter() throws Exception {
    // The filter applies only when the cookie is the sole credential. A bearer token skips it.
    Cookie cookie = registerParentAndGetCookie();

    mockMvc
        .perform(
            post("/api/children")
                .header("Authorization", getAuthorizationHeader(cookie.getValue()))
                .contentType("application/json")
                .content(childBody()))
        .andExpect(status().isCreated());
  }
}
