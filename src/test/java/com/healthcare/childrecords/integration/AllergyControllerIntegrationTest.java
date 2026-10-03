package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.AllergyRequest;
import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.entity.AllergySeverity;
import com.healthcare.childrecords.entity.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AllergyControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testCreateAllergy_Success() throws Exception {
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
            doctorUsername, "TestPass123!", "Test", "Doctor", "20118707867", "doctor@example.com", null, "Pediatrics", "LIC123456", null);

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
        "Allergic",
        "Child",
        "11111111111",
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
    // Create allergy
    AllergyRequest allergyRequest = new AllergyRequest(
        childId,
        "Peanuts",
        AllergySeverity.SEVERE,
        "Anaphylaxis risk",
        LocalDate.now().minusYears(1),
        "Confirmed via allergy test"
    );

    mockMvc
        .perform(
            post("/api/allergies")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(allergyRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.allergen").value("Peanuts"))
        .andExpect(jsonPath("$.severity").value("SEVERE"));
  }

  @Test
  public void testCreateAllergy_ParentForbidden() throws Exception {
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
        "22222222222",
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

    // Try to create allergy as parent (should fail)
    AllergyRequest allergyRequest = new AllergyRequest(
        childId,
        "Shellfish",
        AllergySeverity.MODERATE,
        "Hives and itching",
        null,
        null
    );

    mockMvc
        .perform(
            post("/api/allergies")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(allergyRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testGetAllergiesForChild() throws Exception {
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
            doctorUsername, "TestPass123!", "Test", "Doctor", "20118707867", "doctor@example.com", null, "Pediatrics", "LIC123456", null);

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
        "Multi",
        "Allergic",
        "33333333333",
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
    // Create allergies
    AllergyRequest allergyRequest1 = new AllergyRequest(
        childId,
        "Milk",
        AllergySeverity.MODERATE,
        "Lactose intolerance",
        null,
        null
    );

    mockMvc
        .perform(
            post("/api/allergies")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(allergyRequest1)))
        .andExpect(status().isCreated());

    AllergyRequest allergyRequest2 = new AllergyRequest(
        childId,
        "Eggs",
        AllergySeverity.MILD,
        "Mild rash",
        null,
        null
    );

    mockMvc
        .perform(
            post("/api/allergies")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(allergyRequest2)))
        .andExpect(status().isCreated());

    // Get allergies for child
    mockMvc
        .perform(
            get("/api/allergies/child/" + childId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].allergen", anyOf(equalTo("Milk"), equalTo("Eggs"))));
  }

  @Test
  public void testGetAllergyById() throws Exception {
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
            doctorUsername, "TestPass123!", "Test", "Doctor", "20118707867", "doctor@example.com", null, "Pediatrics", "LIC123456", null);

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
        "Wheat",
        "Allergy",
        "44444444444",
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
    // Create allergy
    AllergyRequest allergyRequest = new AllergyRequest(
        childId,
        "Gluten",
        AllergySeverity.SEVERE,
        "Celiac disease",
        LocalDate.now().minusMonths(6),
        "Diagnosed at age 4"
    );

    String allergyResponse = mockMvc
        .perform(
            post("/api/allergies")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(allergyRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long allergyId = objectMapper.readTree(allergyResponse).get("id").asLong();

    // Get allergy by ID
    mockMvc
        .perform(
            get("/api/allergies/" + allergyId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.allergen").value("Gluten"))
        .andExpect(jsonPath("$.severity").value("SEVERE"))
        .andExpect(jsonPath("$.reaction").value("Celiac disease"));
  }

  @Test
  public void testUpdateAllergy_Success() throws Exception {
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
            doctorUsername, "TestPass123!", "Test", "Doctor", "20118707867", "doctor@example.com", null, "Pediatrics", "LIC123456", null);

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
        "Nut",
        "Allergy",
        "55555555555",
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
    // Create allergy
    AllergyRequest allergyRequest = new AllergyRequest(
        childId,
        "Tree nuts",
        AllergySeverity.MODERATE,
        "Oral itching",
        null,
        "Suspected allergy"
    );

    String allergyResponse = mockMvc
        .perform(
            post("/api/allergies")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(allergyRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long allergyId = objectMapper.readTree(allergyResponse).get("id").asLong();

    // Update allergy
    AllergyRequest updateRequest = new AllergyRequest(
        childId,
        "Tree nuts",
        AllergySeverity.SEVERE,
        "Anaphylaxis risk - oral itching and swelling",
        LocalDate.now(),
        "Confirmed allergy"
    );

    mockMvc
        .perform(
            put("/api/allergies/" + allergyId)
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.severity").value("SEVERE"))
        .andExpect(jsonPath("$.notes").value("Confirmed allergy"));
  }

  @Test
  public void testDeleteAllergy_Success() throws Exception {
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
            doctorUsername, "TestPass123!", "Test", "Doctor", "20118707867", "doctor@example.com", null, "Pediatrics", "LIC123456", null);

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
        "Sesame",
        "Allergy",
        "66666666666",
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
    // Create allergy
    AllergyRequest allergyRequest = new AllergyRequest(
        childId,
        "Sesame",
        AllergySeverity.MILD,
        "Mild rash",
        null,
        "Outgrown allergy - needs removal"
    );

    String allergyResponse = mockMvc
        .perform(
            post("/api/allergies")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(allergyRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long allergyId = objectMapper.readTree(allergyResponse).get("id").asLong();

    // Delete allergy
    mockMvc
        .perform(
            delete("/api/allergies/" + allergyId)
                .header("Authorization", getAuthorizationHeader(doctorToken)))
        .andExpect(status().isNoContent());

    // Verify allergy is deleted (should get 404)
    mockMvc
        .perform(
            get("/api/allergies/" + allergyId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testDeleteAllergy_ParentForbidden() throws Exception {
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
            doctorUsername, "TestPass123!", "Test", "Doctor", "20118707867", "doctor@example.com", null, "Pediatrics", "LIC123456", null);

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
        "77777777777",
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
    // Create allergy
    AllergyRequest allergyRequest = new AllergyRequest(
        childId,
        "Soy",
        AllergySeverity.MILD,
        "Digestive issues",
        null,
        null
    );

    String allergyResponse = mockMvc
        .perform(
            post("/api/allergies")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(allergyRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long allergyId = objectMapper.readTree(allergyResponse).get("id").asLong();

    // Try to delete as parent (should fail)
    mockMvc
        .perform(
            delete("/api/allergies/" + allergyId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isForbidden());
  }
}
