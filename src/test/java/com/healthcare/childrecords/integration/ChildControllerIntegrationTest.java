package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.entity.Gender;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ChildControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testCreateChild_Success() throws Exception {
    // Register parent
    String username = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String token = loginAndGetToken(username, "TestPass123!");

    // Create child
    String amka = uniqueAmka();
    ChildRequest childRequest = new ChildRequest(
        "John",
        "Doe",
        amka,
        "2020-01-15",
        Gender.MALE,
        "O+"
    );

    mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andExpect(jsonPath("$.amka").value(amka))
        .andExpect(jsonPath("$.gender").value("MALE"));
  }

  @Test
  public void testCreateChild_InvalidAmka() throws Exception {
    // Register parent and get token
    String username = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String token = loginAndGetToken(username, "TestPass123!");

    // Create child with invalid AMKA (not 11 digits)
    String invalidJson = """
        {
          "firstName": "John",
          "lastName": "Doe",
          "amka": "123",
          "dateOfBirth": "2020-01-15",
          "gender": "MALE"
        }
        """;

    mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetMyChildren() throws Exception {
    // Register parent
    String username = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String token = loginAndGetToken(username, "TestPass123!");

    // Create a child
    ChildRequest childRequest = new ChildRequest(
        "Jane",
        "Doe",
        uniqueAmka(),
        "2021-06-20",
        Gender.FEMALE,
        "AB-"
    );

    mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated());

    // List children
    mockMvc
        .perform(
            get("/api/children")
                .header("Authorization", getAuthorizationHeader(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].firstName").value("Jane"));
  }

  @Test
  public void testGetChildById_Success() throws Exception {
    // Register parent
    String username = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String token = loginAndGetToken(username, "TestPass123!");

    // Create a child
    ChildRequest childRequest = new ChildRequest(
        "Jack",
        "Smith",
        uniqueAmka(),
        "2019-03-10",
        Gender.MALE,
        "B+"
    );

    String createResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    Long childId = objectMapper.readTree(createResponse).get("id").asLong();

    // Get child by ID
    mockMvc
        .perform(
            get("/api/children/" + childId)
                .header("Authorization", getAuthorizationHeader(token)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Jack"))
        .andExpect(jsonPath("$.id").value(childId));
  }

  @Test
  public void testGetChildById_Unauthorized() throws Exception {
    // Register first parent and create child
    String username1 = "parent1" + System.currentTimeMillis();
    RegisterParentRequest parentRequest1 =
        new RegisterParentRequest(username1, "TestPass123!", "Parent", "One", "parent1@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest1)))
        .andExpect(status().isCreated());

    String token1 = loginAndGetToken(username1, "TestPass123!");

    ChildRequest childRequest = new ChildRequest(
        "Charlie",
        "Brown",
        uniqueAmka(),
        "2020-05-15",
        Gender.MALE,
        "O+"
    );

    String createResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token1))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long childId = objectMapper.readTree(createResponse).get("id").asLong();

    // Register second parent (different user)
    String username2 = "parent2" + System.currentTimeMillis();
    RegisterParentRequest parentRequest2 =
        new RegisterParentRequest(username2, "TestPass123!", "Parent", "Two", "parent2@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest2)))
        .andExpect(status().isCreated());

    String token2 = loginAndGetToken(username2, "TestPass123!");

    // Second parent tries to access first parent's child
    mockMvc
        .perform(
            get("/api/children/" + childId)
                .header("Authorization", getAuthorizationHeader(token2)))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testUpdateChild_Success() throws Exception {
    // Register parent
    String username = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String token = loginAndGetToken(username, "TestPass123!");

    // Create a child
    ChildRequest childRequest = new ChildRequest(
        "Alice",
        "Wonder",
        uniqueAmka(),
        "2021-09-01",
        Gender.FEMALE,
        "AB+"
    );

    String createResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long childId = objectMapper.readTree(createResponse).get("id").asLong();

    // Update child
    ChildRequest updateRequest = new ChildRequest(
        "Alicia",
        "Wonderland",
        uniqueAmka(),
        "2021-09-01",
        Gender.FEMALE,
        "A+"
    );

    mockMvc
        .perform(
            put("/api/children/" + childId)
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Alicia"))
        .andExpect(jsonPath("$.lastName").value("Wonderland"))
        .andExpect(jsonPath("$.bloodType").value("A+"));
  }

  @Test
  public void testDeleteChild_Success() throws Exception {
    // Register parent
    String username = "parent" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "parent@example.com", null, null);

    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());

    String token = loginAndGetToken(username, "TestPass123!");

    // Create a child
    ChildRequest childRequest = new ChildRequest(
        "Bob",
        "Builder",
        uniqueAmka(),
        "2022-01-01",
        Gender.MALE,
        "O-"
    );

    String createResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();

    long childId = objectMapper.readTree(createResponse).get("id").asLong();

    // Delete child
    mockMvc
        .perform(
            delete("/api/children/" + childId)
                .header("Authorization", getAuthorizationHeader(token)))
        .andExpect(status().isNoContent());

    // Verify child is deleted (should get 404 or forbidden)
    mockMvc
        .perform(
            get("/api/children/" + childId)
                .header("Authorization", getAuthorizationHeader(token)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAssignDoctor_Success() throws Exception {
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

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "David",
        "Smith",
        uniqueAmka(),
        "2020-07-20",
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

    long childId = objectMapper.readTree(childResponse).get("id").asLong();

    // Assign doctor to child
    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
  }

  @Test
  public void testRemoveDoctor_Success() throws Exception {
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

    // Create child
    ChildRequest childRequest = new ChildRequest(
        "Eve",
        "Jones",
        uniqueAmka(),
        "2021-11-11",
        Gender.FEMALE,
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

    long childId = objectMapper.readTree(childResponse).get("id").asLong();

    // Assign doctor
    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());

    // Remove doctor
    mockMvc
        .perform(
            delete("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetChildDoctors_ReturnsOnlyConnectedDoctors() throws Exception {
    String parentUsername = "parent" + System.currentTimeMillis();
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                    new RegisterParentRequest(parentUsername, "TestPass123!", "Test", "Parent", "parent@example.com", null, null))))
        .andExpect(status().isCreated());
    String parentToken = loginAndGetToken(parentUsername, "TestPass123!");

    // Connected doctor
    String connectedDoctorResponse = mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                    new RegisterDoctorRequest("doctorA" + System.currentTimeMillis(), "TestPass123!", "Connected", "Doctor",
                        uniqueAmka(), "doctorA@example.com", null, "Pediatrics", "LIC123456", null))))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
    long connectedDoctorId = objectMapper.readTree(connectedDoctorResponse).get("profileId").asLong();

    // Doctor who is not connected to the child
    mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                    new RegisterDoctorRequest("doctorB" + System.currentTimeMillis(), "TestPass123!", "Other", "Doctor",
                        uniqueAmka(), "doctorB@example.com", null, "Dermatology", "LIC654321", null))))
        .andExpect(status().isCreated());

    String childResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(
                    new ChildRequest("Child", "Doctors", uniqueAmka(), "2020-01-15", Gender.FEMALE, "O-"))))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
    long childId = objectMapper.readTree(childResponse).get("id").asLong();

    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + connectedDoctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/api/children/" + childId + "/doctors")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(connectedDoctorId))
        .andExpect(jsonPath("$[0].specialization").value("Pediatrics"));
  }

  @Test
  public void testCreateChild_DuplicateAmka_ReturnsBadRequest() throws Exception {
    String username = "parentdup" + System.currentTimeMillis();
    RegisterParentRequest parentRequest =
        new RegisterParentRequest(username, "TestPass123!", "Test", "Parent", "dup@example.com", null, null);
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(parentRequest)))
        .andExpect(status().isCreated());
    String token = loginAndGetToken(username, "TestPass123!");

    String amka = String.format("%011d", Math.abs(System.nanoTime()) % 100000000000L);
    ChildRequest childRequest = new ChildRequest("Anna", "Doe", amka, "2021-03-10", Gender.FEMALE, null);

    mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(token))
                .content(objectMapper.writeValueAsString(childRequest)))
        .andExpect(status().isBadRequest());
  }
}
