package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProfileControllerIntegrationTest extends BaseIntegrationTest {

  private String registerDoctor(String username, String amka, String email, String license) throws Exception {
    RegisterDoctorRequest request =
        new RegisterDoctorRequest(username, "TestPass123!", "Old", "Name", amka, email, null, "Pediatrics", license, null);
    mockMvc
        .perform(post("/api/auth/register/doctor").contentType("application/json").content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
    return loginAndGetToken(username, "TestPass123!");
  }

  @Test
  public void testUpdateDoctorProfile_UpdatesEveryEditableField() throws Exception {
    String username = "doctor" + System.nanoTime();
    String amka = uniqueAmka();
    String token = registerDoctor(username, amka, username + "@example.com", "LIC" + System.nanoTime());

    mockMvc
        .perform(
            patch("/api/doctors/me")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(
                    """
                    {"firstName":"New","lastName":"Surname","email":"new%s@example.com",
                     "phoneNumber":"2101234567","specialization":"Cardiology","licenseNumber":"NEWLIC%s","address":"Athens"}
                    """
                        .formatted(username, username)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.lastName").value("Surname"))
        .andExpect(jsonPath("$.amka").value(amka))
        .andExpect(jsonPath("$.email").value("new" + username + "@example.com"))
        .andExpect(jsonPath("$.phoneNumber").value("2101234567"))
        .andExpect(jsonPath("$.specialization").value("Cardiology"))
        .andExpect(jsonPath("$.licenseNumber").value("NEWLIC" + username))
        .andExpect(jsonPath("$.address").value("Athens"));

    // The change is stored, not just echoed back
    mockMvc
        .perform(get("/api/doctors/me").header("Authorization", getAuthorizationHeader(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.lastName").value("Surname"));
  }

  @Test
  public void testUpdateDoctorProfile_OmittedFieldsAreUnchanged() throws Exception {
    String username = "doctor" + System.nanoTime();
    String token = registerDoctor(username, uniqueAmka(), username + "@example.com", "LIC" + System.nanoTime());

    mockMvc
        .perform(
            patch("/api/doctors/me")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content("{\"phoneNumber\":\"2109999999\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Old"))
        .andExpect(jsonPath("$.lastName").value("Name"))
        .andExpect(jsonPath("$.specialization").value("Pediatrics"))
        .andExpect(jsonPath("$.phoneNumber").value("2109999999"));
  }

  @Test
  public void testUpdateDoctorProfile_RejectsBlankName() throws Exception {
    String username = "doctor" + System.nanoTime();
    String token = registerDoctor(username, uniqueAmka(), username + "@example.com", "LIC" + System.nanoTime());

    mockMvc
        .perform(
            patch("/api/doctors/me")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content("{\"firstName\":\"  \"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testUpdateDoctorProfile_AmkaCannotBeChanged() throws Exception {
    String username = "doctor" + System.nanoTime();
    String amka = uniqueAmka();
    String token = registerDoctor(username, amka, username + "@example.com", "LIC" + System.nanoTime());

    mockMvc
        .perform(
            patch("/api/doctors/me")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content("{\"amka\":\"" + uniqueAmka() + "\",\"firstName\":\"New\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.amka").value(amka));

    mockMvc
        .perform(get("/api/doctors/me").header("Authorization", getAuthorizationHeader(token)))
        .andExpect(jsonPath("$.amka").value(amka));
  }

  @Test
  public void testUpdateParentProfile_UpdatesNameAndEmail() throws Exception {
    String username = "parent" + System.nanoTime();
    RegisterParentRequest request =
        new RegisterParentRequest(username, "TestPass123!", "Old", "Name", username + "@example.com", null, null);
    mockMvc
        .perform(post("/api/auth/register/parent").contentType("application/json").content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
    String token = loginAndGetToken(username, "TestPass123!");

    mockMvc
        .perform(
            patch("/api/parents/me")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(
                    "{\"firstName\":\"New\",\"lastName\":\"Surname\",\"email\":\"new" + username + "@example.com\",\"phoneNumber\":\"6900000000\",\"address\":\"Athens\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("New"))
        .andExpect(jsonPath("$.lastName").value("Surname"))
        .andExpect(jsonPath("$.email").value("new" + username + "@example.com"))
        .andExpect(jsonPath("$.phoneNumber").value("6900000000"))
        .andExpect(jsonPath("$.address").value("Athens"));
  }
}
