package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.ChangePasswordRequest;
import com.healthcare.childrecords.dto.request.LoginRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerIntegrationTest extends BaseIntegrationTest {

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
        .andExpect(jsonPath("$.token").value(notNullValue()))
        .andExpect(jsonPath("$.role").value("ROLE_PARENT"));
  }

  @Test
  public void testRegisterDoctor_Success() throws Exception {
    String username = "testdoctor" + System.currentTimeMillis();
    RegisterDoctorRequest request =
        new RegisterDoctorRequest(
            username, "TestPass123!", "Test", "Doctor", "20118707867", "doctor@example.com", null, "Pediatrics", "LIC123456", null);

    mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value(notNullValue()))
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
        .andExpect(jsonPath("$.message").value("Username already taken"));
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
        .andExpect(jsonPath("$.token").value(notNullValue()))
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
        .andExpect(jsonPath("$.token").value(notNullValue()));
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
        .andExpect(jsonPath("$.message").value("Current password is incorrect"));
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
        .andExpect(status().isForbidden());
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
}
