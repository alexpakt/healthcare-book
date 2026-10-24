package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.AppointmentRequest;
import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.entity.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AppointmentControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testCreateAppointment_Success() throws Exception {
    // Register parent and doctor
    String parentUsername = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(parentUsername, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String parentToken = loginAndGetToken(parentUsername, "TestPass123!");

    String doctorUsername = "doctor" + System.currentTimeMillis();
    RegisterDoctorRequest doctorRequest =
        new RegisterDoctorRequest(
            doctorUsername, "TestPass123!", "Test", "Doctor", uniqueAmka(), "doctor@example.com", null, "Pediatrics", "LIC123456", null);

    String doctorRegisterResponse = mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(doctorRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Child",
        "One",
        uniqueAmka(),
        "2020-01-15",
        Gender.MALE,
        "O+"
    );

    String childResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long childId = objectMapper.readTree(childResponse).get("id").asLong();

    connectDoctor(parentToken, childId, doctorId);

    // Create appointment
    LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
    AppointmentRequest appointmentRequest = new AppointmentRequest(
        childId,
        doctorId,
        futureDate,
        "Routine checkup",
        "Annual health screening"
    );

    mockMvc
        .perform(
            post("/api/appointments")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reason").value("Routine checkup"))
        .andExpect(jsonPath("$.notes").value("Annual health screening"));
  }

  @Test
  public void testCreateAppointment_PastDateInvalid() throws Exception {
    // Register parent and doctor
    String parentUsername = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(parentUsername, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

    String parentToken = loginAndGetToken(parentUsername, "TestPass123!");

    String doctorUsername = "doctor" + System.currentTimeMillis();
    RegisterDoctorRequest doctorRequest =
        new RegisterDoctorRequest(
            doctorUsername, "TestPass123!", "Test", "Doctor", uniqueAmka(), "doctor@example.com", null, "Pediatrics", "LIC123456", null);

    String doctorRegisterResponse = mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(doctorRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Child",
        "Two",
        uniqueAmka(),
        "2020-01-15",
        Gender.FEMALE,
        "AB+"
    );

    String childResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long childId = objectMapper.readTree(childResponse).get("id").asLong();

    connectDoctor(parentToken, childId, doctorId);

    // Try to create appointment with past date
    LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
    String invalidJson = """
        {
          "childId": %d,
          "doctorId": %d,
          "appointmentDate": "%s",
          "reason": "Past appointment"
        }
        """.formatted(childId, doctorId, pastDate);

    mockMvc
        .perform(
            post("/api/appointments")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetMyAppointments() throws Exception {
    // Register parent and doctor
    String parentUsername = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(parentUsername, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String parentToken = loginAndGetToken(parentUsername, "TestPass123!");

    String doctorUsername = "doctor" + System.currentTimeMillis();
    RegisterDoctorRequest doctorRequest =
        new RegisterDoctorRequest(
            doctorUsername, "TestPass123!", "Test", "Doctor", uniqueAmka(), "doctor@example.com", null, "Pediatrics", "LIC123456", null);

    String doctorRegisterResponse = mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(doctorRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Child",
        "Three",
        uniqueAmka(),
        "2020-01-15",
        Gender.MALE,
        "B-"
    );

    String childResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long childId = objectMapper.readTree(childResponse).get("id").asLong();

    connectDoctor(parentToken, childId, doctorId);

    // Create appointment
    LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
    AppointmentRequest appointmentRequest = new AppointmentRequest(
        childId,
        doctorId,
        futureDate,
        "Checkup",
        null
    );

    mockMvc
        .perform(
            post("/api/appointments")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isCreated());

    // Get my appointments
    mockMvc
        .perform(
            get("/api/appointments")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].reason").value("Checkup"));
  }

  @Test
  public void testGetAppointmentsForChild() throws Exception {
    // Register parent and doctor
    String parentUsername = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(parentUsername, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String parentToken = loginAndGetToken(parentUsername, "TestPass123!");

    String doctorUsername = "doctor" + System.currentTimeMillis();
    RegisterDoctorRequest doctorRequest =
        new RegisterDoctorRequest(
            doctorUsername, "TestPass123!", "Test", "Doctor", uniqueAmka(), "doctor@example.com", null, "Pediatrics", "LIC123456", null);

    String doctorRegisterResponse = mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(doctorRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Child",
        "Four",
        uniqueAmka(),
        "2020-01-15",
        Gender.FEMALE,
        "O-"
    );

    String childResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long childId = objectMapper.readTree(childResponse).get("id").asLong();

    connectDoctor(parentToken, childId, doctorId);

    // Create appointment
    LocalDateTime futureDate = LocalDateTime.now().plusDays(10);
    AppointmentRequest appointmentRequest = new AppointmentRequest(
        childId,
        doctorId,
        futureDate,
        "Vaccination",
        "Routine immunization"
    );

    mockMvc
        .perform(
            post("/api/appointments")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isCreated());

    // Get appointments for child
    mockMvc
        .perform(
            get("/api/appointments/child/" + childId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].reason").value("Vaccination"));
  }

  @Test
  public void testUpdateAppointmentStatus() throws Exception {
    // Register parent and doctor
    String parentUsername = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(parentUsername, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String parentToken = loginAndGetToken(parentUsername, "TestPass123!");

    String doctorUsername = "doctor" + System.currentTimeMillis();
    RegisterDoctorRequest doctorRequest =
        new RegisterDoctorRequest(
            doctorUsername, "TestPass123!", "Test", "Doctor", uniqueAmka(), "doctor@example.com", null, "Pediatrics", "LIC123456", null);

    String doctorRegisterResponse = mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(doctorRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Child",
        "Five",
        uniqueAmka(),
        "2020-01-15",
        Gender.MALE,
        "AB-"
    );

    String childResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long childId = objectMapper.readTree(childResponse).get("id").asLong();

    connectDoctor(parentToken, childId, doctorId);

    // Assign doctor to child
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");
    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());

    // Create appointment
    LocalDateTime futureDate = LocalDateTime.now().plusDays(5);
    AppointmentRequest appointmentRequest = new AppointmentRequest(
        childId,
        doctorId,
        futureDate,
        "Dental checkup",
        null
    );

    String appointmentResponse = mockMvc
        .perform(
            post("/api/appointments")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long appointmentId = objectMapper.readTree(appointmentResponse).get("id").asLong();

    // Update appointment status to SCHEDULED first (as doctor)
    String statusUpdateToScheduled = "{\"status\": \"SCHEDULED\"}";

    mockMvc
        .perform(
            patch("/api/appointments/" + appointmentId + "/status")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(statusUpdateToScheduled))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SCHEDULED"));

    // Update appointment status to COMPLETED
    String statusUpdateToCompleted = "{\"status\": \"COMPLETED\"}";

    mockMvc
        .perform(
            patch("/api/appointments/" + appointmentId + "/status")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(statusUpdateToCompleted))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  private void connectDoctor(String parentToken, Long childId, Long doctorId) throws Exception {
    mockMvc
        .perform(
            post("/api/children/{childId}/doctors/{doctorId}", childId, doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
  }

  @Test
  public void testCreateAppointment_UnconnectedDoctorForbidden() throws Exception {
    String parentUsername = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(parentUsername, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String parentToken = loginAndGetToken(parentUsername, "TestPass123!");

    String doctorUsername = "doctor" + System.currentTimeMillis();
    RegisterDoctorRequest doctorRequest =
        new RegisterDoctorRequest(
            doctorUsername, "TestPass123!", "Test", "Doctor", uniqueAmka(), "doctor@example.com", null, "Pediatrics", "LIC123456", null);

    String doctorRegisterResponse = mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(doctorRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();

    ChildRequest childRequest = new ChildRequest("Child", "Four", uniqueAmka(), "2020-01-15", Gender.MALE, "A+");

    String childResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long childId = objectMapper.readTree(childResponse).get("id").asLong();

    // Doctor is not connected to the child, so the parent must not be able to request an appointment with them
    AppointmentRequest appointmentRequest =
        new AppointmentRequest(childId, doctorId, LocalDateTime.now().plusDays(7), "Checkup", null);

    mockMvc
        .perform(
            post("/api/appointments")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(appointmentRequest)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Doctor is not connected to this child"));
  }
}
