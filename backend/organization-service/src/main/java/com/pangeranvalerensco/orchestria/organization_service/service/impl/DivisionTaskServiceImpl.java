package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import com.pangeranvalerensco.orchestria.organization_service.entity.Division;
import com.pangeranvalerensco.orchestria.organization_service.entity.DivisionTask;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.MemberAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskPriority;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionTaskRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionTaskResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionTaskRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberAssignmentRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.DivisionTaskAccessService;
import com.pangeranvalerensco.orchestria.organization_service.service.DivisionTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DivisionTaskServiceImpl implements DivisionTaskService {

    private final DivisionTaskRepository taskRepository;
    private final DivisionRepository divisionRepository;
    private final MemberRepository memberRepository;
    private final MemberAssignmentRepository memberAssignmentRepository;
    private final DivisionTaskAccessService accessService;

    @Override
    public ApiResponse<List<DivisionTaskResponse>> getAllTasks() {
        if (!accessService.isGlobalManager() && !accessService.isKetuaDivisi()) {
            throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki akses manager.");
        }

        List<DivisionTask> allTasks = taskRepository.findAll();
        List<DivisionTaskResponse> tasks = allTasks.stream()
                .filter(DivisionTask::getActive)
                .filter(task -> {
                    if (accessService.isGlobalManager()) return true;
                    List<Long> managed = accessService.getManagedDivisionIds();
                    return managed != null && managed.contains(task.getDivision().getId());
                })
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<DivisionTaskResponse>>builder()
                .success(true)
                .message("Daftar tugas divisi berhasil diambil")
                .data(tasks)
                .build();
    }

    @Override
    public ApiResponse<List<DivisionTaskResponse>> getTasksByDivision(Long divisionId) {
        accessService.validateManagerAccess(divisionId);
        Division division = findDivisionById(divisionId);

        List<DivisionTaskResponse> tasks = taskRepository
                .findByDivisionAndActiveTrueOrderByDueDateAscCreatedAtDesc(division)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<DivisionTaskResponse>>builder()
                .success(true)
                .message("Daftar tugas berdasarkan divisi berhasil diambil")
                .data(tasks)
                .build();
    }

    @Override
    public ApiResponse<List<DivisionTaskResponse>> getTasksByAssignedMember(Long memberId) {
        Member member = findMemberById(memberId);

        List<DivisionTaskResponse> tasks = taskRepository
                .findByAssignedMemberAndActiveTrueOrderByDueDateAscCreatedAtDesc(member)
                .stream()
                .filter(task -> {
                    if (accessService.isGlobalManager()) return true;
                    List<Long> managed = accessService.getManagedDivisionIds();
                    return managed != null && managed.contains(task.getDivision().getId());
                })
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<DivisionTaskResponse>>builder()
                .success(true)
                .message("Daftar tugas berdasarkan anggota berhasil diambil")
                .data(tasks)
                .build();
    }

    @Override
    public ApiResponse<List<DivisionTaskResponse>> getTasksByStatus(TaskStatus status) {
        List<DivisionTaskResponse> tasks = taskRepository
                .findByStatusAndActiveTrueOrderByDueDateAscCreatedAtDesc(status)
                .stream()
                .filter(task -> {
                    if (accessService.isGlobalManager()) return true;
                    List<Long> managed = accessService.getManagedDivisionIds();
                    return managed != null && managed.contains(task.getDivision().getId());
                })
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<DivisionTaskResponse>>builder()
                .success(true)
                .message("Daftar tugas berdasarkan status berhasil diambil")
                .data(tasks)
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskResponse> getTaskById(Long id) {
        DivisionTask task = findTaskById(id);
        accessService.validateTaskReadAccess(task);

        return ApiResponse.<DivisionTaskResponse>builder()
                .success(true)
                .message("Detail tugas divisi berhasil diambil")
                .data(mapToResponse(task))
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskResponse> createTask(DivisionTaskRequest request) {
        accessService.validateManagerAccess(request.getDivisionId());
        
        Division division = findDivisionById(request.getDivisionId());
        Member assignedMember = null;

        if (request.getAssignedMemberId() != null) {
            assignedMember = findMemberById(request.getAssignedMemberId());
            validateMemberAssignment(assignedMember, division);
        }

        DivisionTask task = DivisionTask.builder()
                .division(division)
                .assignedMember(assignedMember)
                .title(request.getTitle().trim())
                .description(trimOrNull(request.getDescription()))
                .dueDate(request.getDueDate())
                .status(TaskStatus.TODO)
                .priority(defaultIfNull(request.getPriority(), TaskPriority.MEDIUM))
                .active(true)
                .build();

        DivisionTask savedTask = taskRepository.save(task);

        return ApiResponse.<DivisionTaskResponse>builder()
                .success(true)
                .message("Tugas divisi berhasil dibuat")
                .data(mapToResponse(savedTask))
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskResponse> updateTask(Long id, DivisionTaskRequest request) {
        DivisionTask task = findTaskById(id);
        accessService.validateManagerAccess(task.getDivision().getId());
        accessService.validateManagerAccess(request.getDivisionId());

        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.CANCELLED) {
            throw new BadRequestException("Tugas yang sudah DONE atau CANCELLED tidak dapat diubah");
        }
        
        if (request.getStatus() != null && request.getStatus() != task.getStatus()) {
            throw new BadRequestException("Status tugas hanya dapat diubah melalui endpoint perubahan status");
        }

        Division division = findDivisionById(request.getDivisionId());
        Member assignedMember = null;

        if (request.getAssignedMemberId() != null) {
            assignedMember = findMemberById(request.getAssignedMemberId());
            validateMemberAssignment(assignedMember, division);
        }

        task.setDivision(division);
        task.setAssignedMember(assignedMember);
        task.setTitle(request.getTitle().trim());
        task.setDescription(trimOrNull(request.getDescription()));
        task.setDueDate(request.getDueDate());
        task.setPriority(defaultIfNull(request.getPriority(), TaskPriority.MEDIUM));

        DivisionTask savedTask = taskRepository.save(task);

        return ApiResponse.<DivisionTaskResponse>builder()
                .success(true)
                .message("Tugas divisi berhasil diperbarui")
                .data(mapToResponse(savedTask))
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskResponse> updateTaskStatus(Long id, TaskStatus status) {
        DivisionTask task = findTaskById(id);
        accessService.validateManagerAccess(task.getDivision().getId());
        
        TaskStatus currentStatus = task.getStatus();
        if (currentStatus == TaskStatus.DONE || currentStatus == TaskStatus.CANCELLED) {
            throw new BadRequestException("Tugas yang sudah DONE atau CANCELLED tidak dapat diubah statusnya");
        }
        
        boolean validTransition = false;
        if (currentStatus == TaskStatus.TODO) {
            validTransition = (status == TaskStatus.IN_PROGRESS || status == TaskStatus.CANCELLED);
        } else if (currentStatus == TaskStatus.IN_PROGRESS) {
            validTransition = (status == TaskStatus.SUBMITTED || status == TaskStatus.DONE || status == TaskStatus.CANCELLED);
        } else if (currentStatus == TaskStatus.SUBMITTED) {
            validTransition = (status == TaskStatus.IN_PROGRESS || status == TaskStatus.DONE || status == TaskStatus.CANCELLED);
        }
        
        if (!validTransition && currentStatus != status) {
            throw new BadRequestException("Transisi status tidak valid");
        }

        task.setStatus(status);

        DivisionTask savedTask = taskRepository.save(task);

        return ApiResponse.<DivisionTaskResponse>builder()
                .success(true)
                .message("Status tugas divisi berhasil diperbarui")
                .data(mapToResponse(savedTask))
                .build();
    }

    @Override
    public ApiResponse<Void> deleteTask(Long id) {
        DivisionTask task = findTaskById(id);
        accessService.validateManagerAccess(task.getDivision().getId());
        
        task.setActive(false);
        taskRepository.save(task);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Tugas divisi berhasil dinonaktifkan")
                .data(null)
                .build();
    }

    @Override
    public ApiResponse<List<DivisionTaskResponse>> getMyTasks() {
        Member member = accessService.getCurrentMember();
        List<DivisionTaskResponse> tasks = taskRepository
                .findByAssignedMemberAndActiveTrueOrderByDueDateAscCreatedAtDesc(member)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<DivisionTaskResponse>>builder()
                .success(true)
                .message("Daftar tugas milik anggota berhasil diambil")
                .data(tasks)
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskResponse> updateMyTaskStatus(Long id, TaskStatus status) {
        DivisionTask task = findTaskById(id);
        accessService.validateTaskAssignment(task);
        
        if (!task.getActive()) {
            throw new BadRequestException("Tugas tidak aktif");
        }

        // Validate transitions for member
        if (status != TaskStatus.IN_PROGRESS && status != TaskStatus.SUBMITTED) {
            throw new BadRequestException("Anggota hanya dapat mengubah status menjadi IN_PROGRESS atau SUBMITTED");
        }

        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.CANCELLED) {
            throw new BadRequestException("Tugas yang sudah selesai atau dibatalkan tidak dapat diubah oleh anggota");
        }

        if (status == TaskStatus.IN_PROGRESS) {
            if (task.getStatus() != TaskStatus.TODO && task.getStatus() != TaskStatus.SUBMITTED) {
                throw new BadRequestException("Transisi ke IN_PROGRESS tidak valid dari status saat ini");
            }
        }

        if (status == TaskStatus.SUBMITTED) {
            if (task.getStatus() != TaskStatus.IN_PROGRESS) {
                throw new BadRequestException("Hanya tugas IN_PROGRESS yang dapat dikirim (SUBMITTED)");
            }
        }

        task.setStatus(status);
        DivisionTask savedTask = taskRepository.save(task);

        return ApiResponse.<DivisionTaskResponse>builder()
                .success(true)
                .message("Status tugas milik anggota berhasil diperbarui")
                .data(mapToResponse(savedTask))
                .build();
    }

    private void validateMemberAssignment(Member member, Division division) {
        List<MemberAssignment> activeAssignments = memberAssignmentRepository
                .findByMemberAndStatusAndActiveTrueAndPeriodCurrentPeriodTrueAndPeriodActiveTrue(
                        member, AssignmentStatus.ACTIVE);
        
        boolean hasAssignmentInDivision = activeAssignments.stream()
                .anyMatch(a -> a.getDivision().getId().equals(division.getId()));
                
        if (!hasAssignmentInDivision) {
            throw new BadRequestException("Anggota yang dipilih tidak memiliki penugasan aktif di divisi ini pada periode saat ini");
        }
    }

    private DivisionTask findTaskById(Long id) {
        DivisionTask task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tugas divisi tidak ditemukan"));
        if (!task.getActive()) {
            throw new ResourceNotFoundException("Tugas divisi tidak ditemukan");
        }
        return task;
    }

    private Division findDivisionById(Long id) {
        return divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Divisi tidak ditemukan"));
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anggota tidak ditemukan"));
    }

    private DivisionTaskResponse mapToResponse(DivisionTask task) {
        Member assignedMember = task.getAssignedMember();

        return DivisionTaskResponse.builder()
                .id(task.getId())
                .divisionId(task.getDivision().getId())
                .divisionCode(task.getDivision().getCode())
                .divisionName(task.getDivision().getName())
                .assignedMemberId(assignedMember != null ? assignedMember.getId() : null)
                .assignedMemberName(assignedMember != null ? assignedMember.getFullName() : null)
                .assignedMemberEmail(assignedMember != null ? assignedMember.getEmail() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .priority(task.getPriority())
                .active(task.getActive())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}