package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.entity.DoctorChildRequest;
import com.healthcare.childrecords.entity.Gender;
import com.healthcare.childrecords.repository.DoctorChildRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DoctorChildRequestIntegrationTest extends BaseIntegrationTest {

  @Autowired private DoctorChildRequestRepository requestRepository;

  private String parentToken;
  private String doctorToken;
  private long doctorId;
  private long childId;

  @BeforeEach
  void setUp() throws Exception {
    String suffix = String.valueOf(System.nanoTime());

    String parentUsername = "parent" + suffix;
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(
                        new RegisterParentRequest(
                            parentUsername, "TestPass123!", "Test", "Parent", "parent" + suffix + "@example.com", null, null))))
        .andExpect(status().isCreated());
    parentToken = loginAndGetToken(parentUsername, "TestPass123!");

    String doctorUsername = "doctor" + suffix;
    String doctorResponse =
        mockMvc
            .perform(
                post("/api/auth/register/doctor")
                    .contentType("application/json")
                    .content(
                        objectMapper.writeValueAsString(
                            new RegisterDoctorRequest(
                                doctorUsername, "TestPass123!", "Test", "Doctor", uniqueAmka(),
                                "doctor" + suffix + "@example.com", null, "Pediatrics", "LIC" + suffix, null))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    doctorId = objectMapper.readTree(doctorResponse).get("profileId").asLong();
    doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    String childResponse =
        mockMvc
            .perform(
                post("/api/children")
                    .contentType("application/json")
                    .header("Authorization", getAuthorizationHeader(parentToken))
                    .content(
                        objectMapper.writeValueAsString(
                            new ChildRequest("Nick", "Smith", uniqueAmka(), "2020-07-20", Gender.MALE, "A+"))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    childId = objectMapper.readTree(childResponse).get("id").asLong();
  }

  private long sendRequest() throws Exception {
    String response =
        mockMvc
            .perform(
                post("/api/doctor-child-requests")
                    .contentType("application/json")
                    .header("Authorization", getAuthorizationHeader(doctorToken))
                    .content("{\"childId\": " + childId + ", \"notes\": \"please\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    return objectMapper.readTree(response).get("id").asLong();
  }

  private void approve(long requestId) throws Exception {
    mockMvc
        .perform(
            post("/api/doctor-child-requests/" + requestId + "/approve")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"));
  }

  private void removeDoctor() throws Exception {
    mockMvc
        .perform(
            delete("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());
  }

  @Test
  public void testSendRequest_AgainAfterParentRemovedDoctor_ReusesRequestAsPending() throws Exception {
    long firstRequestId = sendRequest();
    approve(firstRequestId);
    removeDoctor();
    org.junit.jupiter.api.Assertions.assertEquals(
        DoctorChildRequest.RequestStatus.REVOKED,
        requestRepository.findById(firstRequestId).orElseThrow().getStatus());

    long secondRequestId = sendRequest();

    org.junit.jupiter.api.Assertions.assertEquals(firstRequestId, secondRequestId);
    mockMvc
        .perform(get("/api/doctor-child-requests/pending").header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(secondRequestId));

    approve(secondRequestId);
    mockMvc
        .perform(get("/api/children/" + childId + "/doctors").header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(doctorId));
  }

  @Test
  public void testSendRequest_WhilePending_IsRejected() throws Exception {
    sendRequest();

    mockMvc
        .perform(
            post("/api/doctor-child-requests")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content("{\"childId\": " + childId + "}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testSendRequest_WhileStillConnected_IsRejected() throws Exception {
    approve(sendRequest());

    mockMvc
        .perform(
            post("/api/doctor-child-requests")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content("{\"childId\": " + childId + "}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testSendRequest_AfterRejection_ReusesRequestAsPending() throws Exception {
    long firstRequestId = sendRequest();
    mockMvc
        .perform(
            post("/api/doctor-child-requests/" + firstRequestId + "/reject")
                .param("reason", "no")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/doctor-child-requests")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content("{\"childId\": " + childId + ", \"notes\": \"second try\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(firstRequestId))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.notes").value("second try"))
        .andExpect(jsonPath("$.resolvedAt").doesNotExist());

    mockMvc
        .perform(get("/api/doctor-child-requests/pending").header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  private String registerOtherParentAndLogin() throws Exception {
    String username = "otherparent" + System.nanoTime();
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(
                    objectMapper.writeValueAsString(
                        new RegisterParentRequest(
                            username, "TestPass123!", "Other", "Parent", username + "@example.com", null, null))))
        .andExpect(status().isCreated());
    return loginAndGetToken(username, "TestPass123!");
  }

  @Test
  public void testSendRequest_CreatesPendingRequestWithDoctorAndChildDetails() throws Exception {
    mockMvc
        .perform(
            post("/api/doctor-child-requests")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content("{\"childId\": " + childId + ", \"notes\": \"please\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.doctorId").value(doctorId))
        .andExpect(jsonPath("$.childId").value(childId))
        .andExpect(jsonPath("$.childFirstName").value("Nick"))
        .andExpect(jsonPath("$.notes").value("please"))
        .andExpect(jsonPath("$.resolvedAt").doesNotExist());
  }

  @Test
  public void testSendRequest_UnknownChild_ReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            post("/api/doctor-child-requests")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content("{\"childId\": 999999999}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testSendRequest_MissingChildId_ReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/doctor-child-requests")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testSendRequest_Unauthenticated_IsRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/doctor-child-requests")
                .contentType("application/json")
                .content("{\"childId\": " + childId + "}"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  public void testGetPendingRequests_ListsOnlyTheParentsOwnPendingRequests() throws Exception {
    long pendingId = sendRequest();
    String otherParentToken = registerOtherParentAndLogin();

    mockMvc
        .perform(get("/api/doctor-child-requests/pending").header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(pendingId))
        .andExpect(jsonPath("$[0].status").value("PENDING"));

    mockMvc
        .perform(get("/api/doctor-child-requests/pending").header("Authorization", getAuthorizationHeader(otherParentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void testGetPendingRequests_ExcludesResolvedRequests() throws Exception {
    approve(sendRequest());

    mockMvc
        .perform(get("/api/doctor-child-requests/pending").header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void testGetSentPendingRequests_ListsTheDoctorsPendingRequestsOnly() throws Exception {
    mockMvc
        .perform(get("/api/doctor-child-requests/pending/sent").header("Authorization", getAuthorizationHeader(doctorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    long requestId = sendRequest();

    mockMvc
        .perform(get("/api/doctor-child-requests/pending/sent").header("Authorization", getAuthorizationHeader(doctorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(requestId));

    approve(requestId);

    mockMvc
        .perform(get("/api/doctor-child-requests/pending/sent").header("Authorization", getAuthorizationHeader(doctorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void testApprove_ConnectsDoctorAndSetsResolvedAt() throws Exception {
    long requestId = sendRequest();

    mockMvc
        .perform(
            post("/api/doctor-child-requests/" + requestId + "/approve")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.resolvedAt").isNotEmpty());

    mockMvc
        .perform(get("/api/children/" + childId + "/doctors").header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(doctorId));
  }

  @Test
  public void testApprove_ByAnotherParent_IsForbiddenAndDoesNotConnectDoctor() throws Exception {
    long requestId = sendRequest();
    String otherParentToken = registerOtherParentAndLogin();

    mockMvc
        .perform(
            post("/api/doctor-child-requests/" + requestId + "/approve")
                .header("Authorization", getAuthorizationHeader(otherParentToken)))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(get("/api/children/" + childId + "/doctors").header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void testApprove_UnknownRequest_ReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            post("/api/doctor-child-requests/999999999/approve")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testReject_StoresReasonAndDoesNotConnectDoctor() throws Exception {
    long requestId = sendRequest();

    mockMvc
        .perform(
            post("/api/doctor-child-requests/" + requestId + "/reject")
                .param("reason", "not my doctor")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"))
        .andExpect(jsonPath("$.notes").value("not my doctor"))
        .andExpect(jsonPath("$.resolvedAt").isNotEmpty());

    mockMvc
        .perform(get("/api/children/" + childId + "/doctors").header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
    mockMvc
        .perform(get("/api/doctor-child-requests/pending").header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void testReject_WithoutReason_Succeeds() throws Exception {
    long requestId = sendRequest();

    mockMvc
        .perform(
            post("/api/doctor-child-requests/" + requestId + "/reject")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"));
  }

  @Test
  public void testReject_ByAnotherParent_IsForbidden() throws Exception {
    long requestId = sendRequest();
    String otherParentToken = registerOtherParentAndLogin();

    mockMvc
        .perform(
            post("/api/doctor-child-requests/" + requestId + "/reject")
                .header("Authorization", getAuthorizationHeader(otherParentToken)))
        .andExpect(status().isForbidden());
    org.junit.jupiter.api.Assertions.assertEquals(
        DoctorChildRequest.RequestStatus.PENDING,
        requestRepository.findById(requestId).orElseThrow().getStatus());
  }

  @Test
  public void testReject_UnknownRequest_ReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            post("/api/doctor-child-requests/999999999/reject")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isNotFound());
  }
}
