package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.dto.request.VaccinationRequest;
import com.healthcare.childrecords.entity.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class VaccinationControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testCreateVaccination_Success() throws Exception {
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
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Vaccinate",
        "Me",
        uniqueAmka(),
        "2023-01-15",
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
    // Create vaccination
    VaccinationRequest vacRequest = new VaccinationRequest(
        childId,
        doctorId,
        "Rotavirus",
        LocalDate.now().minusDays(30),
        "BATCH123",
        "First dose"
    );

    mockMvc
        .perform(
            post("/api/vaccinations")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(vacRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.vaccineName").value("Rotavirus"))
        .andExpect(jsonPath("$.batchNumber").value("BATCH123"));
  }

  @Test
  public void testCreateVaccination_ParentForbidden() throws Exception {
    // Register parent
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

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Child",
        "One",
        uniqueAmka(),
        "2023-01-15",
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

    // Try to create vaccination as parent (should fail)
    VaccinationRequest vacRequest = new VaccinationRequest(
        childId,
        null,
        "Polio",
        LocalDate.now(),
        null,
        null
    );

    mockMvc
        .perform(
            post("/api/vaccinations")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(vacRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testGetVaccinationsForChild() throws Exception {
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
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Vaccinated",
        "Child",
        uniqueAmka(),
        "2023-01-15",
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
    // Create vaccination
    VaccinationRequest vacRequest = new VaccinationRequest(
        childId,
        doctorId,
        "Measles",
        LocalDate.now().minusDays(60),
        "BATCH456",
        "MMR vaccine"
    );

    mockMvc
        .perform(
            post("/api/vaccinations")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(vacRequest)))
        .andExpect(status().isCreated());

    // Get vaccinations for child
    mockMvc
        .perform(
            get("/api/vaccinations/child/" + childId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].vaccineName").value("Measles"));
  }

  @Test
  public void testGetVaccinationById() throws Exception {
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
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Immune",
        "System",
        uniqueAmka(),
        "2023-01-15",
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
    // Create vaccination
    VaccinationRequest vacRequest = new VaccinationRequest(
        childId,
        doctorId,
        "Diphtheria",
        LocalDate.now().minusDays(90),
        "BATCH789",
        "DPT vaccine"
    );

    String vacResponse = mockMvc
        .perform(
            post("/api/vaccinations")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(vacRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long vacId = objectMapper.readTree(vacResponse).get("id").asLong();

    // Get vaccination by ID
    mockMvc
        .perform(
            get("/api/vaccinations/" + vacId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vaccineName").value("Diphtheria"))
        .andExpect(jsonPath("$.notes").value("DPT vaccine"));
  }

  @Test
  public void testUpdateVaccination_Success() throws Exception {
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
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Booster",
        "Shot",
        uniqueAmka(),
        "2023-01-15",
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
    // Create vaccination
    VaccinationRequest vacRequest = new VaccinationRequest(
        childId,
        doctorId,
        "Tetanus",
        LocalDate.now().minusDays(120),
        "BATCH111",
        "Initial dose"
    );

    String vacResponse = mockMvc
        .perform(
            post("/api/vaccinations")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(vacRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long vacId = objectMapper.readTree(vacResponse).get("id").asLong();

    // Update vaccination
    VaccinationRequest updateRequest = new VaccinationRequest(
        childId,
        doctorId,
        "Tetanus",
        LocalDate.now().minusDays(120),
        "BATCH111",
        "Booster dose scheduled"
    );

    mockMvc
        .perform(
            put("/api/vaccinations/" + vacId)
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.notes").value("Booster dose scheduled"));
  }

  @Test
  public void testUpdateVaccination_ParentForbidden() throws Exception {
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
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Child",
        "Two",
        uniqueAmka(),
        "2023-01-15",
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
    // Create vaccination as doctor
    VaccinationRequest vacRequest = new VaccinationRequest(
        childId,
        doctorId,
        "Hepatitis B",
        LocalDate.now().minusDays(150),
        "BATCH222",
        "Newborn vaccination"
    );

    String vacResponse = mockMvc
        .perform(
            post("/api/vaccinations")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(vacRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long vacId = objectMapper.readTree(vacResponse).get("id").asLong();

    // Try to update as parent (should fail)
    VaccinationRequest updateRequest = new VaccinationRequest(
        childId,
        doctorId,
        "Hepatitis B",
        LocalDate.now().minusDays(150),
        "BATCH222",
        "Updated by parent"
    );

    mockMvc
        .perform(
            put("/api/vaccinations/" + vacId)
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isForbidden());
  }
}
