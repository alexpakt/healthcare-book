package com.healthcare.childrecords.integration;

import com.healthcare.childrecords.dto.request.ChildRequest;
import com.healthcare.childrecords.dto.request.RegisterDoctorRequest;
import com.healthcare.childrecords.dto.request.RegisterParentRequest;
import com.healthcare.childrecords.dto.request.VaccinationRequest;
import com.healthcare.childrecords.entity.Gender;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class VaccinationCombinationIntegrationTest extends BaseIntegrationTest {

  private static final String DTAP = "DTaP (Diphtheria, Tetanus, Pertussis)";
  private static final String IPV = "IPV (Inactivated Polio Vaccine)";
  private static final String HEP_B = "HepB (Hepatitis B)";

  /** A child with one parent and one doctor assigned to the child. */
  private record Setup(Long childId, Long doctorId, String doctorToken) {}

  private Setup setupChildWithDoctor() throws Exception {
    String suffix = String.valueOf(System.nanoTime());
    String parentUsername = "parent" + suffix;
    mockMvc
        .perform(
            post("/api/auth/register/parent")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                    new RegisterParentRequest(parentUsername, "TestPass123!", "Test", "Parent", "parent" + suffix + "@example.com", null, null))))
        .andExpect(status().isCreated());
    String parentToken = loginAndGetToken(parentUsername, "TestPass123!");

    String doctorUsername = "doctor" + suffix;
    String doctorResponse = mockMvc
        .perform(
            post("/api/auth/register/doctor")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                    new RegisterDoctorRequest(doctorUsername, "TestPass123!", "Test", "Doctor", "20118707867", "doctor" + suffix + "@example.com", null, "Pediatrics", "LIC" + suffix, null))))
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse()
        .getContentAsString();
    Long doctorId = objectMapper.readTree(doctorResponse).get("profileId").asLong();
    String doctorToken = loginAndGetToken(doctorUsername, "TestPass123!");

    ChildRequest childRequest = new ChildRequest("Vaccinate", "Me", "33333333333", "2026-01-15", Gender.MALE, "O+");
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

    mockMvc
        .perform(
            post("/api/children/" + childId + "/doctors/" + doctorId)
                .header("Authorization", getAuthorizationHeader(parentToken)))
        .andExpect(status().isOk());

    return new Setup(childId, doctorId, doctorToken);
  }

  private VaccinationRequest request(Setup setup, String vaccineName) {
    return new VaccinationRequest(
        setup.childId(),
        setup.doctorId(),
        vaccineName,
        ADMINISTERED,
        "BATCH1",
        null);
  }

  private void record(Setup setup, String vaccineName) throws Exception {
    mockMvc
        .perform(
            post("/api/vaccinations")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(setup.doctorToken()))
                .content(objectMapper.writeValueAsString(request(setup, vaccineName))))
        .andExpect(status().isCreated());
  }

  @Test
  public void testRecordHexavalent_MarksEachComponentAsRecorded() throws Exception {
    Setup setup = setupChildWithDoctor();

    mockMvc
        .perform(
            post("/api/vaccinations")
                .contentType("application/json")
                .header("Authorization", getAuthorizationHeader(setup.doctorToken()))
                .content(objectMapper.writeValueAsString(request(setup, "Hexavalent"))))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/vaccinations/child/" + setup.childId())
                .header("Authorization", getAuthorizationHeader(setup.doctorToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(4))
        .andExpect(jsonPath("$[*].vaccineName", containsInAnyOrder(DTAP, "Hib", IPV, HEP_B)))
        .andExpect(jsonPath("$[*].vaccineName", not(hasItem("Hexavalent"))))
        .andExpect(jsonPath("$[?(@.vaccineName=='Hib')].doseNumber", contains(1)));
  }

  @Test
  public void testRecordCombination_SecondDoseNumbersEachComponentForward() throws Exception {
    Setup setup = setupChildWithDoctor();

    for (int i = 0; i < 2; i++) {
      mockMvc
          .perform(
              post("/api/vaccinations")
                  .contentType("application/json")
                  .header("Authorization", getAuthorizationHeader(setup.doctorToken()))
                  .content(objectMapper.writeValueAsString(request(setup, "Pentavalent"))))
          .andExpect(status().isCreated());
    }

    mockMvc
        .perform(
            get("/api/vaccinations/child/" + setup.childId())
                .header("Authorization", getAuthorizationHeader(setup.doctorToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(6))
        .andExpect(jsonPath("$[?(@.vaccineName=='Hib')].doseNumber", containsInAnyOrder(1, 2)));
  }

  // Doses are recorded 30 days ago. DTaP doses are scheduled at 2, 4, 6 and 12 months, so the next
  // dose is due the schedule gap after the day the dose was given, not after the date of birth.
  private static final LocalDate ADMINISTERED = LocalDate.now().minusDays(30);

  @Test
  public void testNextDoseDate_IsTheScheduleGapAfterTheDoseWasGiven() throws Exception {
    Setup setup = setupChildWithDoctor();
    record(setup, DTAP);

    mockMvc
        .perform(
            get("/api/vaccinations/child/" + setup.childId())
                .header("Authorization", getAuthorizationHeader(setup.doctorToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.vaccineName=='" + DTAP + "')].doseNumber", contains(1)))
        .andExpect(jsonPath("$[?(@.vaccineName=='" + DTAP + "')].nextDoseDate", contains(ADMINISTERED.plusMonths(2).toString())));
  }

  @Test
  public void testNextDoseDate_SkipsDosesAlreadyRecorded() throws Exception {
    Setup setup = setupChildWithDoctor();
    record(setup, DTAP);
    record(setup, DTAP);

    mockMvc
        .perform(
            get("/api/vaccinations/child/" + setup.childId())
                .header("Authorization", getAuthorizationHeader(setup.doctorToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.doseNumber==2 && @.vaccineName=='" + DTAP + "')].nextDoseDate", contains(ADMINISTERED.plusMonths(2).toString())));
  }

  @Test
  public void testNextDoseDate_IsEmptyAfterTheLastScheduledDose() throws Exception {
    Setup setup = setupChildWithDoctor();
    for (int i = 0; i < 4; i++) {
      record(setup, DTAP);
    }

    mockMvc
        .perform(
            get("/api/vaccinations/child/" + setup.childId())
                .header("Authorization", getAuthorizationHeader(setup.doctorToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.doseNumber==4 && @.vaccineName=='" + DTAP + "')].nextDoseDate", hasItem(nullValue())));
  }

  @Test
  public void testNextDoseDate_IsComputedPerComponentOfACombination() throws Exception {
    Setup setup = setupChildWithDoctor();
    record(setup, "Hexavalent");

    mockMvc
        .perform(
            get("/api/vaccinations/child/" + setup.childId())
                .header("Authorization", getAuthorizationHeader(setup.doctorToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.vaccineName=='Hib')].nextDoseDate", contains(ADMINISTERED.plusMonths(2).toString())))
        .andExpect(jsonPath("$[?(@.vaccineName=='" + HEP_B + "')].nextDoseDate", contains(ADMINISTERED.plusMonths(2).toString())));
  }

  @Test
  public void testVaccineCatalog_CombinationListsItsComponents() throws Exception {
    Setup setup = setupChildWithDoctor();

    String body = mockMvc
        .perform(
            get("/api/vaccines")
                .header("Authorization", getAuthorizationHeader(setup.doctorToken())))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    JsonNode hexavalent = null;
    JsonNode varicella = null;
    for (JsonNode vaccine : objectMapper.readTree(body)) {
      if ("Hexavalent".equals(vaccine.get("vaccineName").asText())) {
        hexavalent = vaccine;
      }
      if ("Varicella".equals(vaccine.get("vaccineName").asText())) {
        varicella = vaccine;
      }
    }

    assertNotNull(hexavalent);
    assertNotNull(varicella);
    assertEquals(4, hexavalent.get("componentVaccineNames").size());
    assertEquals(0, varicella.get("componentVaccineNames").size());
  }
}
