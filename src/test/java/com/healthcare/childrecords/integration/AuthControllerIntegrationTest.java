package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.ChangePasswordRequest;
import com.healthcare.childrecords.dto.request.LoginRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired private JwtUtil jwtUtil;

  @Test
  public void testRegisterParent_Success() throws Exception {
    String username = "testparent" + System.currentTimeMillis();
    RegisterParentRequest request =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "test@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").doesNotExist())
        .andExpect(cookie().httpOnly("access_token", true))
        .andExpect(jsonPath("$.role").value("ROLE_PARENT"));
  }

  @Test
  public void testRegisterDoctor_Success() throws Exception {
    String username = "testdoctor" + System.currentTimeMillis();
    RegisterDoctorRequest request =
        new RegisterDoctorRequest(
            username, "TestPass123!", "Test", "Doctor", uniqueAmka(), "doctor@example.com", null, "Pediatrics", "LIC123456", null);

    mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").doesNotExist())
        .andExpect(cookie().httpOnly("access_token", true))
        .andExpect(jsonPath("$.role").value("ROLE_DOCTOR"));
  }

  @Test
  public void testRegisterParent_MissingRequiredFields() throws Exception {
    String invalidJson = "{\"username\":\"testparent\"}";

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400));
  }

  @Test
  public void testRegisterParent_DuplicateUsername() throws Exception {
    String username = "duplicateuser" + System.currentTimeMillis();
    RegisterParentRequest request =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "test@example.com", null, null);

    // First registration should succeed
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // Second registration with same username should fail with 400 (Username already taken)
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Το όνομα χρήστη χρησιμοποιείται ήδη"));
  }

  @Test
  public void testLogin_Success() throws Exception {
    // First register a user
    String username = "logintest" + System.currentTimeMillis();
    RegisterParentRequest registerRequest =
        new RegisterParentRequest(username, "TestPass123!", "Test", "User", "login@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated());

    // Then login with the same credentials
    LoginRequest loginRequest = new LoginRequest(username, "TestPass123!");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").doesNotExist())
        .andExpect(cookie().httpOnly("access_token", true))
        .andExpect(jsonPath("$.role").value("ROLE_PARENT"));
  }

  @Test
  public void testLogin_InvalidCredentials() throws Exception {
    LoginRequest loginRequest = new LoginRequest("nonexistentuser", "wrongpassword");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void testLogin_MissingFields() throws Exception {
    String invalidJson = "{\"username\":\"testuser\"}";

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testChangePassword_Success() throws Exception {
    // Register a user
    String username = "changepasstest" + System.currentTimeMillis();
    RegisterParentRequest registerRequest =
        new RegisterParentRequest(username, "OldPass123!", "Test", "User", "change@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated());

    // Get authentication token
    String token = loginAndGetToken(username, "OldPass123!");

    // Change password
    ChangePasswordRequest changeRequest = new ChangePasswordRequest("OldPass123!", "NewPass456!");

    mockMvc
        .perform(
            post("/api/auth/change-password")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(changeRequest)))
        .andExpect(status().isOk());

    // Verify old password no longer works
    LoginRequest oldPasswordLogin = new LoginRequest(username, "OldPass123!");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(oldPasswordLogin)))
        .andExpect(status().isUnauthorized());

    // Verify new password works
    LoginRequest newPasswordLogin = new LoginRequest(username, "NewPass456!");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(newPasswordLogin)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").doesNotExist())
        .andExpect(cookie().httpOnly("access_token", true));
  }

  @Test
  public void testChangePassword_IncorrectCurrentPassword() throws Exception {
    // Register a user
    String username = "wrongcurrentpass" + System.currentTimeMillis();
    RegisterParentRequest registerRequest =
        new RegisterParentRequest(username, "CorrectPass123!", "Test", "User", "wrong@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated());

    // Get authentication token
    String token = loginAndGetToken(username, "CorrectPass123!");

    // Try to change password with wrong current password
    ChangePasswordRequest changeRequest = new ChangePasswordRequest("WrongPass123!", "NewPass456!");

    mockMvc
        .perform(
            post("/api/auth/change-password")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(changeRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Ο τρέχων κωδικός πρόσβασης είναι λανθασμένος"));
  }

  @Test
  public void testChangePassword_InvalidNewPassword() throws Exception {
    // Register a user
    String username = "invalidnewpass" + System.currentTimeMillis();
    RegisterParentRequest registerRequest =
        new RegisterParentRequest(username, "ValidPass123!", "Test", "User", "invalid@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated());

    // Get authentication token
    String token = loginAndGetToken(username, "ValidPass123!");

    // Try to change password with new password less than 8 characters
    ChangePasswordRequest changeRequest = new ChangePasswordRequest("ValidPass123!", "Short1!");

    mockMvc
        .perform(
            post("/api/auth/change-password")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(changeRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testChangePassword_Unauthenticated() throws Exception {
    ChangePasswordRequest changeRequest = new ChangePasswordRequest("OldPass123!", "NewPass456!");

    mockMvc
        .perform(
            post("/api/auth/change-password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(changeRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void testChangePassword_MissingFields() throws Exception {
    // Register a user and get token
    String username = "missingfields" + System.currentTimeMillis();
    RegisterParentRequest registerRequest =
        new RegisterParentRequest(username, "TestPass123!", "Test", "User", "missing@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated());

    String token = loginAndGetToken(username, "TestPass123!");

    // Try with missing new password
    String invalidJson = "{\"currentPassword\":\"TestPass123!\"}";

    mockMvc
        .perform(
            post("/api/auth/change-password")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testRegisterDoctor_DuplicateAmka_ReturnsBadRequest() throws Exception {
    String amka = String.format("%011d", Math.abs(System.nanoTime()) % 100000000000L);
    RegisterDoctorRequest first = new RegisterDoctorRequest(
        "docone" + System.currentTimeMillis(), "TestPass123!", "Ελένη", "Κωνσταντίνου", amka,
        "doc1@example.com", null, "Παιδίατρος", "LIC-1", null);
    RegisterDoctorRequest second = new RegisterDoctorRequest(
        "doctwo" + System.currentTimeMillis(), "TestPass123!", "Γιώργος", "Παππάς", amka,
        "doc2@example.com", null, "Παιδίατρος", "LIC-2", null);

    mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(first)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(second)))
        .andExpect(status().isBadRequest());
  }

  private String registerParentAndGetUsername() throws Exception {
    String username = "cookieuser" + System.nanoTime();
    RegisterParentRequest request =
        new RegisterParentRequest(username, "TestPass123!", "Cookie", "User", "cookie@example.com", null, null);
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
    return username;
  }

  private Cookie loginAndGetCookie(String username) throws Exception {
    return mockMvc
        .perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new LoginRequest(username, "TestPass123!"))))
        .andExpect(status().isOk())
        .andExpect(cookie().httpOnly("access_token", true))
        .andExpect(cookie().path("access_token", "/"))
        .andExpect(cookie().attribute("access_token", "SameSite", "Lax"))
        .andReturn()
        .getResponse()
        .getCookie("access_token");
  }

  @Test
  public void testRegister_SetsHttpOnlyCookie() throws Exception {
    String username = "regcookie" + System.nanoTime();
    RegisterParentRequest request =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "t@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(cookie().httpOnly("access_token", true));
  }

  @Test
  public void testMe_WithCookie_ReturnsCurrentUser() throws Exception {
    String username = registerParentAndGetUsername();
    Cookie cookie = loginAndGetCookie(username);

    mockMvc
        .perform(get("/api/auth/me").cookie(cookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value(username))
        .andExpect(jsonPath("$.role").value("ROLE_PARENT"))
        .andExpect(jsonPath("$.profileId").value(notNullValue()));
  }

  @Test
  public void testMe_WithoutCredentials_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
  }

  @Test
  public void testLogout_ClearsCookie() throws Exception {
    mockMvc
        .perform(post("/api/auth/logout"))
        .andExpect(status().isNoContent())
        .andExpect(cookie().maxAge("access_token", 0));
  }

  @Test
  public void testCookieAuth_StateChangingRequestFromUnknownOrigin_IsForbidden() throws Exception {
    Cookie cookie = loginAndGetCookie(registerParentAndGetUsername());
    String body = objectMapper.writeValueAsString(new ChangePasswordRequest("TestPass123!", "NewPass123!"));

    mockMvc
        .perform(
            post("/api/auth/change-password")
                .cookie(cookie)
                .header("Origin", "https://evil.example")
                .contentType("application/json")
                .content(body))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            post("/api/auth/change-password")
                .cookie(cookie)
                .contentType("application/json")
                .content(body))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testCookieAuth_StateChangingRequestFromAllowedOrigin_Succeeds() throws Exception {
    Cookie cookie = loginAndGetCookie(registerParentAndGetUsername());

    mockMvc
        .perform(
            post("/api/auth/change-password")
                .cookie(cookie)
                .header("Origin", "http://localhost:5173")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new ChangePasswordRequest("TestPass123!", "NewPass123!"))))
        .andExpect(status().isOk());
  }

  @Test
  public void testRegisterDoctor_WithTokenForRemovedUser_IsNotRejected() throws Exception {
    UserDetails removedUser =
        org.springframework.security.core.userdetails.User.withUsername("removed" + System.nanoTime())
            .password("unused")
            .roles("PARENT")
            .build();
    String staleToken = jwtUtil.generateToken(removedUser);
    RegisterDoctorRequest request =
        new RegisterDoctorRequest(
            "staledoc" + System.nanoTime(), "TestPass123!", "Test", "Doctor", uniqueAmka(),
            "stale@example.com", null, "Pediatrics", "LIC-STALE", null);

    mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .header("Authorization", getAuthorizationHeader(staleToken))
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }
}
