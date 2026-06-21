package com.pangeranvalerensco.orchestria.organization_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.organization_service.entity.EnglishActivity;
import com.pangeranvalerensco.orchestria.organization_service.entity.EnglishDeposit;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishActivityStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishDepositStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.english.EnglishActivityRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.english.EnglishDepositRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.english.EnglishDepositVerificationRequest;
import com.pangeranvalerensco.orchestria.organization_service.repository.EnglishActivityRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.EnglishDepositRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EnglishIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EnglishActivityRepository activityRepository;

    @Autowired
    private EnglishDepositRepository depositRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .fullName("Test Member")
                .email("member@example.com")
                .active(true)
                .build();
        memberRepository.save(testMember);
    }

    @AfterEach
    void tearDown() {
        depositRepository.deleteAll();
        activityRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"english.activity.manage"})
    void shouldCreateActivity() throws Exception {
        String req = String.format("""
                {
                    "title": "Speech Week 1",
                    "activityDate": "%s",
                    "startTime": "08:00:00",
                    "endTime": "10:00:00",
                    "topic": "Introduction",
                    "description": "Desc",
                    "status": "PUBLISHED"
                }
                """, LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/organization/english/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Speech Week 1"));

        assertEquals(1, activityRepository.count());
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"english.activity.manage"})
    void createActivity_EndTimeBeforeStartTime_ShouldFail() throws Exception {
        String req = String.format("""
                {
                    "title": "Speech Week 1",
                    "activityDate": "%s",
                    "startTime": "10:00:00",
                    "endTime": "08:00:00",
                    "topic": "Introduction",
                    "description": "Desc",
                    "status": "PUBLISHED"
                }
                """, LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/organization/english/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "member@example.com", authorities = {"english.deposit.create"})
    void shouldCreateDeposit() throws Exception {
        EnglishActivity activity = EnglishActivity.builder()
                .title("Speech")
                .activityDate(LocalDate.now())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .topic("Intro")
                .status(EnglishActivityStatus.PUBLISHED)
                .createdByEmail("admin@example.com")
                .active(true)
                .build();
        activityRepository.save(activity);

        String req = String.format("""
                {
                    "activityId": "%s",
                    "topic": "My Topic",
                    "evidenceUrl": "http://example.com/video.mp4",
                    "submissionNote": "Note"
                }
                """, activity.getId());

        mockMvc.perform(post("/api/organization/english/deposits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.topic").value("My Topic"));

        assertEquals(1, depositRepository.count());
    }

    @Test
    @WithMockUser(username = "member@example.com", authorities = {"english.deposit.create"})
    void createDeposit_OnDraftActivity_ShouldFail() throws Exception {
        EnglishActivity activity = EnglishActivity.builder()
                .title("Speech")
                .activityDate(LocalDate.now())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .topic("Intro")
                .status(EnglishActivityStatus.DRAFT)
                .createdByEmail("admin@example.com")
                .active(true)
                .build();
        activityRepository.save(activity);

        String req = String.format("""
                {
                    "activityId": "%s",
                    "topic": "My Topic",
                    "evidenceUrl": "http://example.com/video.mp4",
                    "submissionNote": "Note"
                }
                """, activity.getId());

        mockMvc.perform(post("/api/organization/english/deposits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "member@example.com", authorities = {"english.deposit.create"})
    void createDeposit_Duplicate_ShouldFail() throws Exception {
        EnglishActivity activity = EnglishActivity.builder()
                .title("Speech")
                .activityDate(LocalDate.now())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .topic("Intro")
                .status(EnglishActivityStatus.PUBLISHED)
                .createdByEmail("admin@example.com")
                .active(true)
                .build();
        activityRepository.save(activity);

        EnglishDeposit deposit = EnglishDeposit.builder()
                .activity(activity)
                .memberId(testMember.getId())
                .memberName(testMember.getFullName())
                .memberEmail(testMember.getEmail())
                .topic("T1")
                .evidenceUrl("http://example.com")
                .status(EnglishDepositStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .active(true)
                .build();
        depositRepository.save(deposit);

        String req = String.format("""
                {
                    "activityId": "%s",
                    "topic": "T2",
                    "evidenceUrl": "http://example.com/2",
                    "submissionNote": "Note"
                }
                """, activity.getId());

        mockMvc.perform(post("/api/organization/english/deposits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"english.deposit.verify"})
    void shouldVerifyDeposit_Verified() throws Exception {
        EnglishActivity activity = EnglishActivity.builder()
                .title("Speech")
                .activityDate(LocalDate.now())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .topic("Intro")
                .status(EnglishActivityStatus.PUBLISHED)
                .createdByEmail("admin@example.com")
                .active(true)
                .build();
        activityRepository.save(activity);

        EnglishDeposit deposit = EnglishDeposit.builder()
                .activity(activity)
                .memberId(testMember.getId())
                .memberName(testMember.getFullName())
                .memberEmail(testMember.getEmail())
                .topic("T1")
                .evidenceUrl("http://example.com")
                .status(EnglishDepositStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .active(true)
                .build();
        depositRepository.save(deposit);

        String req = """
                {
                    "decision": "VERIFIED",
                    "score": 85.5,
                    "verificationNote": "Good job"
                }
                """;

        mockMvc.perform(post("/api/organization/english/deposits/" + deposit.getId() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.score").value(85.5));
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"english.deposit.verify"})
    void shouldVerifyDeposit_Rejected() throws Exception {
        EnglishActivity activity = EnglishActivity.builder()
                .title("Speech")
                .activityDate(LocalDate.now())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .topic("Intro")
                .status(EnglishActivityStatus.PUBLISHED)
                .createdByEmail("admin@example.com")
                .active(true)
                .build();
        activityRepository.save(activity);

        EnglishDeposit deposit = EnglishDeposit.builder()
                .activity(activity)
                .memberId(testMember.getId())
                .memberName(testMember.getFullName())
                .memberEmail(testMember.getEmail())
                .topic("T1")
                .evidenceUrl("http://example.com")
                .status(EnglishDepositStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .active(true)
                .build();
        depositRepository.save(deposit);

        String req = """
                {
                    "decision": "REJECTED",
                    "verificationNote": "Bad audio"
                }
                """;

        mockMvc.perform(post("/api/organization/english/deposits/" + deposit.getId() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"english.deposit.verify"})
    void verifyDeposit_RejectedWithoutNote_ShouldFail() throws Exception {
        EnglishActivity activity = EnglishActivity.builder()
                .title("Speech")
                .activityDate(LocalDate.now())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .topic("Intro")
                .status(EnglishActivityStatus.PUBLISHED)
                .createdByEmail("admin@example.com")
                .active(true)
                .build();
        activityRepository.save(activity);

        EnglishDeposit deposit = EnglishDeposit.builder()
                .activity(activity)
                .memberId(testMember.getId())
                .memberName(testMember.getFullName())
                .memberEmail(testMember.getEmail())
                .topic("T1")
                .evidenceUrl("http://example.com")
                .status(EnglishDepositStatus.SUBMITTED)
                .submittedAt(LocalDateTime.now())
                .active(true)
                .build();
        depositRepository.save(deposit);

        String req = """
                {
                    "decision": "REJECTED",
                    "verificationNote": ""
                }
                """;

        mockMvc.perform(post("/api/organization/english/deposits/" + deposit.getId() + "/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(req))
                .andExpect(status().isBadRequest());
    }
}
