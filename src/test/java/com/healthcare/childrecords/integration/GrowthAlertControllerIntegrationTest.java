package com.healthcare.childrecords.integration;

import com.fasterxml.jackson.databind.JsonNode;
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

public class GrowthAlertControllerIntegrationTest extends BaseIntegrationTest {

  private static final String PASSWORD = "TestPass123!";

  @Test
  public void testCrossingAlert_acknowledgementFlowAndAccessControl() throws Exception {
    String suffix = String.valueOf(System.nanoTime());

    String parentUsername = "alertparent" + suffix;
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                    new RegisterParentRequest(parentUsername, PASSWORD, "Alert", "Parent", "alert" + suffix + "@example.com", null, null))))
        .andExpect(status().isCreated());
    String parentToken = loginAndGetToken(parentUsername, PASSWORD);

    String doctorUsername = "alertdoctor" + suffix;
    String doctorResponse = mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                    new RegisterDoctorRequest(doctorUsername, PASSWORD, "Alert", "Doctor", "20118707867", "doc" + suffix + "@example.com", null, "Pediatrics", "LIC" + suffix, null))))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
    long doctorId = objectMapper.readTree(doctorResponse).get("profileId").asLong();
    String doctorToken = loginAndGetToken(doctorUsername, PASSWORD);

    String otherDoctorUsername = "otherdoctor" + suffix;
    mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                    new RegisterDoctorRequest(otherDoctorUsername, PASSWORD, "Other", "Doctor", "20118707868", "other" + suffix + "@example.com", null, "Pediatrics", "LIX" + suffix, null))))
        .andExpect(status().isCreated());
    String otherDoctorToken = loginAndGetToken(otherDoctorUsername, PASSWORD);

    String childResponse = mockMvc
        .perform(
            post("/api/children")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content(objectMapper.writeValueAsString(
                    new ChildRequest("Alert", "Child", "33333333333", "2020-01-15", Gender.MALE, "O+"))))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
    long childId = objectMapper.readTree(childResponse).get("id").asLong();

    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());

    // Pick heights from the seeded WHO curve: the 3rd percentile at 6 months and the
    // 50th percentile at 12 months. Moving from the 3rd to the 50th crosses two major lines.
    JsonNode curve = objectMapper.readTree(
        mockMvc
            .perform(
                get("/api/growth-measurements/references/child/" + childId + "/type/HEIGHT")
                    .header("Authorization", getAuthorizationHeader(doctorToken)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString());
    double heightAt6Months = referenceValue(curve, 6, 3);
    double heightAt12Months = referenceValue(curve, 12, 50);

    recordHeight(doctorToken, childId, LocalDate.of(2020, 7, 15), heightAt6Months);
    recordHeight(doctorToken, childId, LocalDate.of(2021, 1, 15), heightAt12Months);

    // Alerts are visible to the assigned doctor
    String alertsResponse = mockMvc
        .perform(
            get("/api/growth-measurements/child/" + childId + "/alerts")
                .header("Authorization", getAuthorizationHeader(doctorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$[0].ruleType").value("PERCENTILE_CROSSING"))
        .andExpect(jsonPath("$[0].measurementType").value("HEIGHT"))
        .andExpect(jsonPath("$[0].acknowledged").value(false))
        .andExpect(jsonPath("$[0].reason").value(containsString("For clinical review")))
        .andReturn()
        .getResponse()
        .getContentAsString();
    String alertKey = objectMapper.readTree(alertsResponse).get(0).get("alertKey").asText();

    // Parents and unassigned doctors cannot read alerts
    mockMvc
        .perform(
            get("/api/growth-measurements/child/" + childId + "/alerts")
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(
            get("/api/growth-measurements/child/" + childId + "/alerts")
                .header("Authorization", getAuthorizationHeader(otherDoctorToken)))
        .andExpect(status().isForbidden());

    // Parents cannot acknowledge
    mockMvc
        .perform(
            post("/api/growth-measurements/child/" + childId + "/alerts/" + alertKey + "/acknowledge")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(parentToken))
                .content("{}"))
        .andExpect(status().isForbidden());

    // Assigned doctor acknowledges with a note
    mockMvc
        .perform(
            post("/api/growth-measurements/child/" + childId + "/alerts/" + alertKey + "/acknowledge")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content("{\"note\":\"Reviewed with parent\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.acknowledged").value(true))
        .andExpect(jsonPath("$.acknowledgementNote").value("Reviewed with parent"));

    // Acknowledging again is idempotent and keeps the original note
    mockMvc
        .perform(
            post("/api/growth-measurements/child/" + childId + "/alerts/" + alertKey + "/acknowledge")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content("{\"note\":\"Second attempt\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.acknowledgementNote").value("Reviewed with parent"));

    // The alert stays acknowledged on the next read
    mockMvc
        .perform(
            get("/api/growth-measurements/child/" + childId + "/alerts")
                .header("Authorization", getAuthorizationHeader(doctorToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].acknowledged").value(true))
        .andExpect(jsonPath("$[0].acknowledgedBy").value("Alert Doctor"));

    // Unknown alert key is rejected
    mockMvc
        .perform(
            post("/api/growth-measurements/child/" + childId + "/alerts/" + "0".repeat(64) + "/acknowledge")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content("{}"))
        .andExpect(status().isNotFound());
  }

  private void recordHeight(String doctorToken, long childId, LocalDate date, double heightCm) throws Exception {
    GrowthMeasurementRequest request = new GrowthMeasurementRequest(childId, date, heightCm, null, null, null);
    mockMvc
        .perform(
            post("/api/growth-measurements")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(doctorToken))
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }

  private double referenceValue(JsonNode curve, int ageInMonths, int percentile) {
    for (JsonNode row : curve) {
      if (row.get("ageInMonths").asInt() == ageInMonths && row.get("percentile").asInt() == percentile) {
        return row.get("value").asDouble();
      }
    }
    throw new IllegalStateException("No reference row for age " + ageInMonths + " percentile " + percentile);
  }
}
