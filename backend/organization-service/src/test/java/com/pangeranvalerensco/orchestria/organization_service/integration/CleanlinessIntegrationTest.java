package com.pangeranvalerensco.orchestria.organization_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.organization_service.entity.*;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AttendanceStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.PointRecordType;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.ScheduleStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness.AttendanceRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness.PointRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness.ScheduleRequest;
import com.pangeranvalerensco.orchestria.organization_service.repository.*;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CleanlinessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CleanlinessScheduleRepository scheduleRepository;

    @Autowired
    private CleanlinessAssignmentRepository assignmentRepository;

    @Autowired
    private CleanlinessPointRecordRepository pointRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private Member testMember1;
    private Member testMember2;

    @BeforeEach
    void setUp() {
        testMember1 = Member.builder()
                .fullName("Member Satu")
                .email("member1@example.com")
                .active(true)
                .build();
        testMember1 = memberRepository.save(testMember1);

        testMember2 = Member.builder()
                .fullName("Member Dua")
                .email("member2@example.com")
                .active(true)
                .build();
        testMember2 = memberRepository.save(testMember2);
    }

    @AfterEach
    void tearDown() {
        assignmentRepository.deleteAll();
        pointRepository.deleteAll();
        scheduleRepository.deleteAll();
        memberRepository.deleteAll();
    }

    // 1. manager berhasil membuat schedule dan assignments
    @Test
    @WithMockUser(username = "manager@example.com", authorities = {"cleanliness.schedule.manage"})
    void managerCanCreateScheduleAndAssignments() throws Exception {
        String jsonRequest = String.format(
                "{\"title\":\"Piket Pagi\",\"dutyDate\":\"%s\",\"startTime\":\"08:00:00\",\"endTime\":\"10:00:00\",\"location\":\"Sekretariat\",\"description\":\"Membersihkan sekre\",\"status\":\"PUBLISHED\",\"memberIds\":[%d]}",
                LocalDate.now().toString(), testMember1.getId()
        );

        mockMvc.perform(post("/api/organization/cleanliness/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Piket Pagi"))
                .andExpect(jsonPath("$.data.assignments[0].memberId").value(testMember1.getId()));
    }

    // 2. anggota tidak dapat membuat schedule
    @Test
    @WithMockUser(username = "member1@example.com", authorities = {"cleanliness.schedule.read"})
    void anggotaCannotCreateSchedule() throws Exception {
        String jsonRequest = String.format(
                "{\"title\":\"Piket Pagi\",\"dutyDate\":\"%s\",\"startTime\":\"08:00:00\",\"endTime\":\"10:00:00\",\"location\":\"Sekretariat\",\"description\":\"\",\"status\":\"PUBLISHED\",\"memberIds\":[]}",
                LocalDate.now().toString()
        );

        mockMvc.perform(post("/api/organization/cleanliness/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isForbidden());
    }

    // 3. anggota hanya melihat schedule miliknya
    @Test
    @WithMockUser(username = "member1@example.com", authorities = {"cleanliness.schedule.read"})
    void anggotaCanOnlySeeOwnPublishedSchedules() throws Exception {
        CleanlinessSchedule s1 = scheduleRepository.save(CleanlinessSchedule.builder()
                .title("My Schedule").dutyDate(LocalDate.now()).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0))
                .location("Sekre").status(ScheduleStatus.PUBLISHED).createdByEmail("manager@test.com").active(true).build());
        
        assignmentRepository.save(CleanlinessAssignment.builder()
                .schedule(s1).memberId(testMember1.getId()).memberName("Member Satu").memberEmail("member1@example.com")
                .attendanceStatus(AttendanceStatus.PENDING).active(true).build());

        CleanlinessSchedule s2 = scheduleRepository.save(CleanlinessSchedule.builder()
                .title("Other Schedule").dutyDate(LocalDate.now()).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0))
                .location("Sekre").status(ScheduleStatus.PUBLISHED).createdByEmail("manager@test.com").active(true).build());

        mockMvc.perform(get("/api/organization/cleanliness/schedules/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("My Schedule"));
    }

    // 4. non-assigned member tidak dapat membuka detail
    @Test
    @WithMockUser(username = "member2@example.com", authorities = {"cleanliness.schedule.read"})
    void nonAssignedMemberCannotViewScheduleDetail() throws Exception {
        CleanlinessSchedule s1 = scheduleRepository.save(CleanlinessSchedule.builder()
                .title("My Schedule").dutyDate(LocalDate.now()).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0))
                .location("Sekre").status(ScheduleStatus.PUBLISHED).createdByEmail("manager@test.com").active(true).build());

        assignmentRepository.save(CleanlinessAssignment.builder()
                .schedule(s1).memberId(testMember1.getId()).memberName("Member Satu").memberEmail("member1@example.com")
                .attendanceStatus(AttendanceStatus.PENDING).active(true).build());

        mockMvc.perform(get("/api/organization/cleanliness/schedules/" + s1.getId()))
                .andExpect(status().isForbidden());
    }

    // 5. anggota berhasil mencatat attendance sendiri
    @Test
    @WithMockUser(username = "member1@example.com", authorities = {"cleanliness.attendance.create"})
    void memberCanRecordOwnAttendance() throws Exception {
        CleanlinessSchedule s1 = scheduleRepository.save(CleanlinessSchedule.builder()
                .title("My Schedule").dutyDate(LocalDate.now()).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0))
                .location("Sekre").status(ScheduleStatus.PUBLISHED).createdByEmail("manager@test.com").active(true).build());

        CleanlinessAssignment a1 = assignmentRepository.save(CleanlinessAssignment.builder()
                .schedule(s1).memberId(testMember1.getId()).memberName("Member Satu").memberEmail("member1@example.com")
                .attendanceStatus(AttendanceStatus.PENDING).active(true).build());

        AttendanceRequest request = new AttendanceRequest(AttendanceStatus.PRESENT, "Hadir", "http://bukti.com");

        mockMvc.perform(post("/api/organization/cleanliness/assignments/" + a1.getId() + "/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attendanceStatus").value("PRESENT"));
    }

    // 6. anggota tidak dapat mencatat attendance orang lain
    @Test
    @WithMockUser(username = "member2@example.com", authorities = {"cleanliness.attendance.create"})
    void memberCannotRecordOtherAttendance() throws Exception {
        CleanlinessSchedule s1 = scheduleRepository.save(CleanlinessSchedule.builder()
                .title("My Schedule").dutyDate(LocalDate.now()).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0))
                .location("Sekre").status(ScheduleStatus.PUBLISHED).createdByEmail("manager@test.com").active(true).build());

        CleanlinessAssignment a1 = assignmentRepository.save(CleanlinessAssignment.builder()
                .schedule(s1).memberId(testMember1.getId()).memberName("Member Satu").memberEmail("member1@example.com")
                .attendanceStatus(AttendanceStatus.PENDING).active(true).build());

        AttendanceRequest request = new AttendanceRequest(AttendanceStatus.PRESENT, "Hadir", "http://bukti.com");

        mockMvc.perform(post("/api/organization/cleanliness/assignments/" + a1.getId() + "/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // 7. schedule CANCELLED tidak menerima attendance
    @Test
    @WithMockUser(username = "member1@example.com", authorities = {"cleanliness.attendance.create"})
    void cancelledScheduleRejectsAttendance() throws Exception {
        CleanlinessSchedule s1 = scheduleRepository.save(CleanlinessSchedule.builder()
                .title("My Schedule").dutyDate(LocalDate.now()).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(10, 0))
                .location("Sekre").status(ScheduleStatus.CANCELLED).createdByEmail("manager@test.com").active(true).build());

        CleanlinessAssignment a1 = assignmentRepository.save(CleanlinessAssignment.builder()
                .schedule(s1).memberId(testMember1.getId()).memberName("Member Satu").memberEmail("member1@example.com")
                .attendanceStatus(AttendanceStatus.PENDING).active(true).build());

        AttendanceRequest request = new AttendanceRequest(AttendanceStatus.PRESENT, "Hadir", "http://bukti.com");

        mockMvc.perform(post("/api/organization/cleanliness/assignments/" + a1.getId() + "/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tidak dapat mengubah presensi pada jadwal yang sudah selesai atau dibatalkan"));
    }

    // 8. REWARD dengan nilai negatif ditolak
    @Test
    @WithMockUser(username = "manager@example.com", authorities = {"cleanliness.point.manage"})
    void negativeRewardPointRejected() throws Exception {
        PointRequest request = new PointRequest(testMember1.getId(), null, PointRecordType.REWARD, -5, "Testing");

        mockMvc.perform(post("/api/organization/cleanliness/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 9. VIOLATION tanpa alasan ditolak
    @Test
    @WithMockUser(username = "manager@example.com", authorities = {"cleanliness.violation.manage"})
    void violationWithoutReasonRejected() throws Exception {
        PointRequest request = new PointRequest(testMember1.getId(), null, PointRecordType.VIOLATION, 5, "");

        mockMvc.perform(post("/api/organization/cleanliness/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 10. report summary menghitung reward, violation, dan net point dengan benar
    @Test
    @WithMockUser(username = "manager@example.com", authorities = {"cleanliness.report.read"})
    void reportSummaryCalculatesPointsCorrectly() throws Exception {
        pointRepository.save(CleanlinessPointRecord.builder()
                .memberId(testMember1.getId()).memberName("Member Satu").type(PointRecordType.REWARD).pointValue(10)
                .reason("Rajin").recordedByEmail("m@test.com").recordedAt(LocalDateTime.now()).active(true).build());
        pointRepository.save(CleanlinessPointRecord.builder()
                .memberId(testMember1.getId()).memberName("Member Satu").type(PointRecordType.VIOLATION).pointValue(3)
                .reason("Telat").recordedByEmail("m@test.com").recordedAt(LocalDateTime.now()).active(true).build());

        mockMvc.perform(get("/api/organization/cleanliness/reports/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRewardPoints").value(10))
                .andExpect(jsonPath("$.data.totalViolationPoints").value(3))
                .andExpect(jsonPath("$.data.netPoints").value(7))
                .andExpect(jsonPath("$.data.memberLeaderboard[0].netPoints").value(7));
    }
}



