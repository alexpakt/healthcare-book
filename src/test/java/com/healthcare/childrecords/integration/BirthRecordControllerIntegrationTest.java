package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.BirthRecordRequest;
import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.entity.DeliveryType;
import com.healthcare.childrecords.entity.Gender;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class BirthRecordControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testCreateBirthRecord_Success() throws Exception {
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
        "Newborn",
        "Baby",
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
    // Create birth record
    BirthRecordRequest birthRequest = new BirthRecordRequest(
        childId,
        3500,
        50.0,
        35.0,
        39,
        DeliveryType.VAGINAL,
        9,
        9,
        "City General Hospital",
        false,
        null,
        "Healthy newborn"
    );

    mockMvc
        .perform(
            post("/api/birth-records")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(birthRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.birthWeightGrams").value(3500))
        .andExpect(jsonPath("$.birthLengthCm").value(50.0))
        .andExpect(jsonPath("$.apgarScore1Min").value(9))
        .andExpect(jsonPath("$.apgarScore5Min").value(9));
  }

  @Test
  public void testCreateBirthRecord_ParentForbidden() throws Exception {
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

    // Try to create birth record as parent (should fail)
    BirthRecordRequest birthRequest = new BirthRecordRequest(
        childId,
        3200,
        48.0,
        34.0,
        38,
        DeliveryType.CESAREAN,
        8,
        9,
        "Hospital",
        false,
        null,
        null
    );

    mockMvc
        .perform(
            post("/api/birth-records")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(birthRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testGetBirthRecordByChildId() throws Exception {
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
        "Birth",
        "Record",
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
    // Create birth record
    BirthRecordRequest birthRequest = new BirthRecordRequest(
        childId,
        3800,
        51.0,
        36.0,
        40,
        DeliveryType.VAGINAL,
        9,
        9,
        "St. Mary's Hospital",
        false,
        null,
        "Full-term, normal delivery"
    );

    mockMvc
        .perform(
            post("/api/birth-records")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(birthRequest)))
        .andExpect(status().isCreated());

    // Get birth record by child ID
    mockMvc
        .perform(
            get("/api/birth-records/child/" + childId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.birthWeightGrams").value(3800))
        .andExpect(jsonPath("$.birthLengthCm").value(51.0))
        .andExpect(jsonPath("$.birthHospital").value("St. Mary's Hospital"));
  }

  @Test
  public void testGetBirthRecord_NotFound() throws Exception {
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

    // Create child without birth record
    ChildRequest childRequest = new ChildRequest(
        "No",
        "Birth",
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

    long childId = objectMapper.readTree(childResponse).get("id").asLong();

    // Try to get non-existent birth record
    mockMvc
        .perform(
            get("/api/birth-records/child/" + childId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testUpdateBirthRecord_Success() throws Exception {
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
        "Birth",
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
    // Create birth record
    BirthRecordRequest birthRequest = new BirthRecordRequest(
        childId,
        3400,
        50.0,
        35.0,
        39,
        DeliveryType.CESAREAN,
        8,
        9,
        "Local Hospital",
        true,
        2,
        "Initial NICU admission"
    );

    mockMvc
        .perform(
            post("/api/birth-records")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(birthRequest)))
        .andExpect(status().isCreated());

    // Update birth record
    BirthRecordRequest updateRequest = new BirthRecordRequest(
        childId,
        3400,
        50.0,
        35.0,
        39,
        DeliveryType.CESAREAN,
        8,
        9,
        "Local Hospital",
        true,
        1,
        "NICU discharge after 1 day"
    );

    mockMvc
        .perform(
            put("/api/birth-records/child/" + childId)
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nicuDurationDays").value(1))
        .andExpect(jsonPath("$.notes").value("NICU discharge after 1 day"));
  }

  @Test
  public void testUpdateBirthRecord_ParentForbidden() throws Exception {
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
    // Create birth record as doctor
    BirthRecordRequest birthRequest = new BirthRecordRequest(
        childId,
        3300,
        49.0,
        34.5,
        38,
        DeliveryType.VAGINAL,
        9,
        9,
        "County Hospital",
        false,
        null,
        "Normal delivery"
    );

    mockMvc
        .perform(
            post("/api/birth-records")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(birthRequest)))
        .andExpect(status().isCreated());

    // Try to update as parent (should fail)
    BirthRecordRequest updateRequest = new BirthRecordRequest(
        childId,
        3300,
        49.0,
        34.5,
        38,
        DeliveryType.VAGINAL,
        9,
        9,
        "County Hospital",
        false,
        null,
        "Updated by parent"
    );

    mockMvc
        .perform(
            put("/api/birth-records/child/" + childId)
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testCreateBirthRecord_WithNICU() throws Exception {
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
        "Premature",
        "Baby",
        uniqueAmka(),
        "2023-01-15",
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
    // Create birth record with NICU
    BirthRecordRequest birthRequest = new BirthRecordRequest(
        childId,
        2200,
        45.0,
        32.0,
        35,
        DeliveryType.CESAREAN,
        7,
        8,
        "Neonatal ICU Hospital",
        true,
        5,
        "Premature infant, respiratory support needed"
    );

    mockMvc
        .perform(
            post("/api/birth-records")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(birthRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.nicuAdmission").value(true))
        .andExpect(jsonPath("$.nicuDurationDays").value(5));
  }
}
