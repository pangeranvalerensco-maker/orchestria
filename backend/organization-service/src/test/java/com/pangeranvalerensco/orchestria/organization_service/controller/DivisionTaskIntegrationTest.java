package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.organization_service.entity.*;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.*;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionTaskEvidenceRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionTaskRequest;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DivisionTaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private DivisionRepository divisionRepository;

    @Autowired
    private OrganizationPeriodRepository periodRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private MemberAssignmentRepository assignmentRepository;

    @Autowired
    private DivisionTaskRepository taskRepository;

    @Autowired
    private DivisionTaskEvidenceRepository evidenceRepository;

    private Division divA;
    private Division divB;
    private Member ketuaDivA;
    private Member memberDivA;
    private Member memberDivB;
    private OrganizationPeriod period;

    @BeforeEach
    void setUp() {
        cleanDb();

        period = periodRepository.save(OrganizationPeriod.builder()
                .name("Period Test")
                .startDate(LocalDate.now())
                .currentPeriod(true)
                .active(true)
                .publicVisible(true)
                .build());

        divA = divisionRepository.save(Division.builder()
                .code("DIV-A").name("Divisi A").active(true).build());
        divB = divisionRepository.save(Division.builder()
                .code("DIV-B").name("Divisi B").active(true).build());

        Position posKetua = positionRepository.save(Position.builder()
                .code("KETUA_DIV").name("Ketua Divisi").levelOrder(2).active(true).build());
        Position posAnggota = positionRepository.save(Position.builder()
                .code("ANGGOTA").name("Anggota").levelOrder(5).active(true).build());

        ketuaDivA = memberRepository.save(Member.builder()
                .email("ketuaA@test.com").fullName("Ketua A").status(MemberStatus.ACTIVE).active(true).build());
        memberDivA = memberRepository.save(Member.builder()
                .email("memberA@test.com").fullName("Member A").status(MemberStatus.ACTIVE).active(true).build());
        memberDivB = memberRepository.save(Member.builder()
                .email("memberB@test.com").fullName("Member B").status(MemberStatus.ACTIVE).active(true).build());

        assignmentRepository.save(MemberAssignment.builder()
                .member(ketuaDivA).division(divA).position(posKetua).period(period)
                .status(AssignmentStatus.ACTIVE).active(true).build());
        assignmentRepository.save(MemberAssignment.builder()
                .member(memberDivA).division(divA).position(posAnggota).period(period)
                .status(AssignmentStatus.ACTIVE).active(true).build());
        assignmentRepository.save(MemberAssignment.builder()
                .member(memberDivB).division(divB).position(posAnggota).period(period)
                .status(AssignmentStatus.ACTIVE).active(true).build());
    }

    @AfterEach
    void tearDown() {
        cleanDb();
    }

    private void cleanDb() {
        evidenceRepository.deleteAll();
        taskRepository.deleteAll();
        assignmentRepository.deleteAll();
        positionRepository.deleteAll();
        divisionRepository.deleteAll();
        periodRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"ROLE_SUPER_ADMIN", "division.task.manage"})
    void globalManagerCanCreateTaskForAnyDivision() throws Exception {
        DivisionTaskRequest request = new DivisionTaskRequest();
        request.setDivisionId(divB.getId());
        request.setAssignedMemberId(memberDivB.getId());
        request.setTitle("Task for B");

        mockMvc.perform(post("/api/organization/division-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Task for B"));
    }

    @Test
    @WithMockUser(username = "ketuaA@test.com", authorities = {"ROLE_KETUA_DIVISI", "division.task.manage"})
    void ketuaDivisiCanCreateTaskForOwnDivision() throws Exception {
        DivisionTaskRequest request = new DivisionTaskRequest();
        request.setDivisionId(divA.getId());
        request.setAssignedMemberId(memberDivA.getId());
        request.setTitle("Task for A");

        mockMvc.perform(post("/api/organization/division-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("Task for A"));
    }

    @Test
    @WithMockUser(username = "ketuaA@test.com", authorities = {"ROLE_KETUA_DIVISI", "division.task.manage"})
    void ketuaDivisiCannotCreateTaskForOtherDivision() throws Exception {
        DivisionTaskRequest request = new DivisionTaskRequest();
        request.setDivisionId(divB.getId());
        request.setAssignedMemberId(memberDivB.getId());
        request.setTitle("Task for B");

        mockMvc.perform(post("/api/organization/division-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ketuaA@test.com", authorities = {"ROLE_KETUA_DIVISI", "division.task.manage"})
    void cannotAssignMemberFromDifferentDivision() throws Exception {
        DivisionTaskRequest request = new DivisionTaskRequest();
        request.setDivisionId(divA.getId());
        request.setAssignedMemberId(memberDivB.getId()); // member B is not in Div A
        request.setTitle("Task for B in A");

        mockMvc.perform(post("/api/organization/division-tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "memberA@test.com", authorities = {"ROLE_ANGGOTA", "division.task.read"})
    void memberCanViewOwnTasks() throws Exception {
        DivisionTask task = taskRepository.save(DivisionTask.builder()
                .division(divA).assignedMember(memberDivA).title("My Task")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).active(true).build());

        mockMvc.perform(get("/api/organization/division-tasks/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(task.getId()));
    }

    @Test
    @WithMockUser(username = "memberA@test.com", authorities = {"ROLE_ANGGOTA", "division.task.read"})
    void memberCanUpdateStatusTodoToInProgress() throws Exception {
        DivisionTask task = taskRepository.save(DivisionTask.builder()
                .division(divA).assignedMember(memberDivA).title("My Task")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).active(true).build());

        mockMvc.perform(patch("/api/organization/division-tasks/" + task.getId() + "/my-status/IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser(username = "memberA@test.com", authorities = {"ROLE_ANGGOTA", "division.task.read"})
    void memberCanUpdateStatusInProgressToSubmitted() throws Exception {
        DivisionTask task = taskRepository.save(DivisionTask.builder()
                .division(divA).assignedMember(memberDivA).title("My Task")
                .status(TaskStatus.IN_PROGRESS).priority(TaskPriority.MEDIUM).active(true).build());

        mockMvc.perform(patch("/api/organization/division-tasks/" + task.getId() + "/my-status/SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    }

    @Test
    @WithMockUser(username = "memberB@test.com", authorities = {"ROLE_ANGGOTA", "division.task.read"})
    void memberCannotUpdateOtherMembersTask() throws Exception {
        DivisionTask task = taskRepository.save(DivisionTask.builder()
                .division(divA).assignedMember(memberDivA).title("My Task")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).active(true).build());

        mockMvc.perform(patch("/api/organization/division-tasks/" + task.getId() + "/my-status/IN_PROGRESS"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "memberA@test.com", authorities = {"ROLE_ANGGOTA", "division.task.read"})
    void memberCanCreateEvidence() throws Exception {
        DivisionTask task = taskRepository.save(DivisionTask.builder()
                .division(divA).assignedMember(memberDivA).title("My Task")
                .status(TaskStatus.IN_PROGRESS).priority(TaskPriority.MEDIUM).active(true).build());

        DivisionTaskEvidenceRequest req = new DivisionTaskEvidenceRequest();
        req.setTaskId(task.getId());
        req.setType(EvidenceType.NOTE);
        req.setTitle("My Note");
        req.setDescription("Work done");

        mockMvc.perform(post("/api/organization/division-task-evidences/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("My Note"));
    }

    @Test
    @WithMockUser(username = "memberB@test.com", authorities = {"ROLE_ANGGOTA", "division.task.read"})
    void memberCannotCreateEvidenceForOtherTask() throws Exception {
        DivisionTask task = taskRepository.save(DivisionTask.builder()
                .division(divA).assignedMember(memberDivA).title("My Task")
                .status(TaskStatus.IN_PROGRESS).priority(TaskPriority.MEDIUM).active(true).build());

        DivisionTaskEvidenceRequest req = new DivisionTaskEvidenceRequest();
        req.setTaskId(task.getId());
        req.setType(EvidenceType.NOTE);
        req.setTitle("My Note");

        mockMvc.perform(post("/api/organization/division-task-evidences/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "memberA@test.com", authorities = {"ROLE_ANGGOTA", "division.task.read"})
    void memberCannotUpdateDoneTask() throws Exception {
        DivisionTask task = taskRepository.save(DivisionTask.builder()
                .division(divA).assignedMember(memberDivA).title("My Task")
                .status(TaskStatus.DONE).priority(TaskPriority.MEDIUM).active(true).build());

        DivisionTaskEvidenceRequest req = new DivisionTaskEvidenceRequest();
        req.setTaskId(task.getId());
        req.setType(EvidenceType.NOTE);
        req.setTitle("Late Note");

        mockMvc.perform(post("/api/organization/division-task-evidences/my")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "ketuaA@test.com", authorities = {"ROLE_KETUA_DIVISI", "division.task.manage"})
    void managerCanUpdateStatusToDone() throws Exception {
        DivisionTask task = taskRepository.save(DivisionTask.builder()
                .division(divA).assignedMember(memberDivA).title("My Task")
                .status(TaskStatus.SUBMITTED).priority(TaskPriority.MEDIUM).active(true).build());

        mockMvc.perform(patch("/api/organization/division-tasks/" + task.getId() + "/status/DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DONE"));
    }

    @Test
    @WithMockUser(username = "ketuaA@test.com", authorities = {"ROLE_KETUA_DIVISI", "division.task.manage"})
    void deleteIsSoftDelete() throws Exception {
        DivisionTask task = taskRepository.save(DivisionTask.builder()
                .division(divA).assignedMember(memberDivA).title("My Task")
                .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).active(true).build());

        mockMvc.perform(delete("/api/organization/division-tasks/" + task.getId()))
                .andExpect(status().isOk());

        DivisionTask deletedTask = taskRepository.findById(task.getId()).orElse(null);
        assertThat(deletedTask).isNotNull();
        assertThat(deletedTask.getActive()).isFalse();
    }
}
