package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.request.GrowthMeasurementRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.entity.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class GrowthMeasurementControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testCreateGrowthMeasurement_Success() throws Exception {
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

    long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Growing",
        "Child",
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

    // Assign doctor to child (required for doctor to create records)
    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
    // Create growth measurement
    GrowthMeasurementRequest measurementRequest = new GrowthMeasurementRequest(
        childId,
        LocalDate.now().minusDays(30),
        75.5,
        12.5,
        45.2,
        "Normal growth"
    );

    mockMvc
        .perform(
            post("/api/growth-measurements")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(measurementRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.heightCm").value(75.5))
        .andExpect(jsonPath("$.weightKg").value(12.5))
        .andExpect(jsonPath("$.headCircumferenceCm").value(45.2));
  }

  @Test
  public void testCreateGrowthMeasurement_ParentForbidden() throws Exception {
    // Register parent
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

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Child",
        "One",
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

    // Try to create measurement as parent (should fail)
    GrowthMeasurementRequest measurementRequest = new GrowthMeasurementRequest(
        childId,
        LocalDate.now(),
        70.0,
        11.0,
        43.0,
        null
    );

    mockMvc
        .perform(
            post("/api/growth-measurements")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(measurementRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testGetGrowthMeasurementsForChild() throws Exception {
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

    long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Tracking",
        "Growth",
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

    // Assign doctor to child (required for doctor to create records)
    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
    // Create multiple measurements
    GrowthMeasurementRequest measurement1 = new GrowthMeasurementRequest(
        childId,
        LocalDate.now().minusDays(60),
        70.0,
        10.5,
        42.0,
        "First measurement"
    );

    mockMvc
        .perform(
            post("/api/growth-measurements")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(measurement1)))
        .andExpect(status().isCreated());

    GrowthMeasurementRequest measurement2 = new GrowthMeasurementRequest(
        childId,
        LocalDate.now().minusDays(30),
        75.5,
        12.0,
        44.0,
        "Second measurement"
    );

    mockMvc
        .perform(
            post("/api/growth-measurements")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(measurement2)))
        .andExpect(status().isCreated());

    // Get measurements for child
    mockMvc
        .perform(
            get("/api/growth-measurements/child/" + childId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].weightKg", anyOf(equalTo(10.5), equalTo(12.0))));
  }

  @Test
  public void testGetGrowthMeasurementById() throws Exception {
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

    long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Measure",
        "Me",
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

    // Assign doctor to child (required for doctor to create records)
    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
    // Create measurement
    GrowthMeasurementRequest measurementRequest = new GrowthMeasurementRequest(
        childId,
        LocalDate.now().minusDays(45),
        68.0,
        10.0,
        41.5,
        "Regular checkup measurement"
    );

    String measurementResponse = mockMvc
        .perform(
            post("/api/growth-measurements")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(measurementRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long measurementId = objectMapper.readTree(measurementResponse).get("id").asLong();

    // Get measurement by ID
    mockMvc
        .perform(
            get("/api/growth-measurements/" + measurementId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.heightCm").value(68.0))
        .andExpect(jsonPath("$.weightKg").value(10.0))
        .andExpect(jsonPath("$.notes").value("Regular checkup measurement"));
  }

  @Test
  public void testUpdateGrowthMeasurement_Success() throws Exception {
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

    long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Update",
        "Growth",
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

    // Assign doctor to child (required for doctor to create records)
    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
    // Create measurement
    GrowthMeasurementRequest measurementRequest = new GrowthMeasurementRequest(
        childId,
        LocalDate.now().minusDays(30),
        72.0,
        11.0,
        43.0,
        "Initial measurement"
    );

    String measurementResponse = mockMvc
        .perform(
            post("/api/growth-measurements")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(measurementRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long measurementId = objectMapper.readTree(measurementResponse).get("id").asLong();

    // Update measurement
    GrowthMeasurementRequest updateRequest = new GrowthMeasurementRequest(
        childId,
        LocalDate.now().minusDays(30),
        72.5,
        11.2,
        43.5,
        "Corrected measurement"
    );

    mockMvc
        .perform(
            put("/api/growth-measurements/" + measurementId)
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.heightCm").value(72.5))
        .andExpect(jsonPath("$.notes").value("Corrected measurement"));
  }

  @Test
  public void testDeleteGrowthMeasurement_Success() throws Exception {
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

    long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Delete",
        "Growth",
        uniqueAmka(),
        "2020-01-15",
        Gender.FEMALE,
        "A+"
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

    // Assign doctor to child (required for doctor to create records)
    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
    // Create measurement
    GrowthMeasurementRequest measurementRequest = new GrowthMeasurementRequest(
        childId,
        LocalDate.now().minusDays(15),
        76.0,
        13.0,
        45.0,
        "To be deleted"
    );

    String measurementResponse = mockMvc
        .perform(
            post("/api/growth-measurements")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(measurementRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long measurementId = objectMapper.readTree(measurementResponse).get("id").asLong();

    // Delete measurement
    mockMvc
        .perform(
            delete("/api/growth-measurements/" + measurementId)
                .header("Authorization", getAuthorizationHeader(doctorToken)))
        .andExpect(status().isNoContent());

    // Verify measurement is deleted
    mockMvc
        .perform(
            get("/api/growth-measurements/" + measurementId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testDeleteGrowthMeasurement_ParentForbidden() throws Exception {
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

    long doctorId = objectMapper.readTree(doctorRegisterResponse).get("profileId").asLong();
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Child",
        "Two",
        uniqueAmka(),
        "2020-01-15",
        Gender.MALE,
        "B+"
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

    // Assign doctor to child (required for doctor to create records)
    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
    // Create measurement
    GrowthMeasurementRequest measurementRequest = new GrowthMeasurementRequest(
        childId,
        LocalDate.now().minusDays(20),
        74.0,
        12.5,
        44.5,
        "Protected measurement"
    );

    String measurementResponse = mockMvc
        .perform(
            post("/api/growth-measurements")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(measurementRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long measurementId = objectMapper.readTree(measurementResponse).get("id").asLong();

    // Try to delete as parent (should fail)
    mockMvc
        .perform(
            delete("/api/growth-measurements/" + measurementId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isForbidden());
  }
}
