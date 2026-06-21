package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import com.pangeranvalerensco.orchestria.organization_service.entity.DivisionTask;
import com.pangeranvalerensco.orchestria.organization_service.entity.DivisionTaskEvidence;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EvidenceType;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionTaskEvidenceRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionTaskEvidenceResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionTaskEvidenceRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionTaskRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.DivisionTaskAccessService;
import com.pangeranvalerensco.orchestria.organization_service.service.DivisionTaskEvidenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DivisionTaskEvidenceServiceImpl implements DivisionTaskEvidenceService {

    private final DivisionTaskEvidenceRepository evidenceRepository;
    private final DivisionTaskRepository taskRepository;
    private final DivisionTaskAccessService accessService;

    @Override
    public ApiResponse<List<DivisionTaskEvidenceResponse>> getEvidencesByTask(Long taskId) {
        DivisionTask task = findTaskById(taskId);
        accessService.validateTaskReadAccess(task);

        List<DivisionTaskEvidenceResponse> evidences = evidenceRepository
                .findByTaskAndActiveTrueOrderByCreatedAtDesc(task)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<DivisionTaskEvidenceResponse>>builder()
                .success(true)
                .message("Daftar bukti tugas divisi berhasil diambil")
                .data(evidences)
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskEvidenceResponse> getEvidenceById(Long id) {
        DivisionTaskEvidence evidence = findEvidenceById(id);
        accessService.validateTaskReadAccess(evidence.getTask());

        return ApiResponse.<DivisionTaskEvidenceResponse>builder()
                .success(true)
                .message("Detail bukti tugas divisi berhasil diambil")
                .data(mapToResponse(evidence))
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskEvidenceResponse> createEvidence(DivisionTaskEvidenceRequest request) {
        DivisionTask task = findTaskById(request.getTaskId());
        accessService.validateManagerAccess(task.getDivision().getId());

        validateEvidenceRequest(request);

        DivisionTaskEvidence evidence = buildEvidenceFromRequest(task, request);
        evidence.setSubmittedByMemberId(null);
        DivisionTaskEvidence savedEvidence = evidenceRepository.save(evidence);

        return ApiResponse.<DivisionTaskEvidenceResponse>builder()
                .success(true)
                .message("Bukti tugas divisi berhasil dibuat")
                .data(mapToResponse(savedEvidence))
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskEvidenceResponse> updateEvidence(Long id, DivisionTaskEvidenceRequest request) {
        DivisionTaskEvidence evidence = findEvidenceById(id);
        DivisionTask task = findTaskById(request.getTaskId());
        
        accessService.validateManagerAccess(evidence.getTask().getDivision().getId());
        accessService.validateManagerAccess(task.getDivision().getId());

        validateEvidenceRequest(request);
        updateEvidenceFields(evidence, task, request);

        DivisionTaskEvidence savedEvidence = evidenceRepository.save(evidence);

        return ApiResponse.<DivisionTaskEvidenceResponse>builder()
                .success(true)
                .message("Bukti tugas divisi berhasil diperbarui")
                .data(mapToResponse(savedEvidence))
                .build();
    }

    @Override
    public ApiResponse<Void> deleteEvidence(Long id) {
        DivisionTaskEvidence evidence = findEvidenceById(id);
        accessService.validateManagerAccess(evidence.getTask().getDivision().getId());

        evidence.setActive(false);
        evidenceRepository.save(evidence);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Bukti tugas divisi berhasil dinonaktifkan")
                .data(null)
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskEvidenceResponse> createMyEvidence(DivisionTaskEvidenceRequest request) {
        DivisionTask task = findTaskById(request.getTaskId());
        accessService.validateTaskAssignment(task);
        validateTaskIsMutableByMember(task);
        validateEvidenceRequest(request);

        DivisionTaskEvidence evidence = buildEvidenceFromRequest(task, request);
        evidence.setSubmittedByMemberId(accessService.getCurrentMember().getId());
        DivisionTaskEvidence savedEvidence = evidenceRepository.save(evidence);

        return ApiResponse.<DivisionTaskEvidenceResponse>builder()
                .success(true)
                .message("Bukti tugas milik anggota berhasil dibuat")
                .data(mapToResponse(savedEvidence))
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskEvidenceResponse> updateMyEvidence(Long id, DivisionTaskEvidenceRequest request) {
        DivisionTaskEvidence evidence = findEvidenceById(id);
        accessService.validateTaskAssignment(evidence.getTask());
        validateTaskIsMutableByMember(evidence.getTask());

        DivisionTask task = findTaskById(request.getTaskId());
        accessService.validateTaskAssignment(task);
        validateTaskIsMutableByMember(task);
        
        Long currentMemberId = accessService.getCurrentMember().getId();
        if (evidence.getSubmittedByMemberId() == null || !evidence.getSubmittedByMemberId().equals(currentMemberId)) {
            throw new org.springframework.security.access.AccessDeniedException("Anggota tidak dapat mengubah bukti milik manager atau anggota lain");
        }

        validateEvidenceRequest(request);
        updateEvidenceFields(evidence, task, request);

        DivisionTaskEvidence savedEvidence = evidenceRepository.save(evidence);

        return ApiResponse.<DivisionTaskEvidenceResponse>builder()
                .success(true)
                .message("Bukti tugas milik anggota berhasil diperbarui")
                .data(mapToResponse(savedEvidence))
                .build();
    }

    @Override
    public ApiResponse<Void> deleteMyEvidence(Long id) {
        DivisionTaskEvidence evidence = findEvidenceById(id);
        accessService.validateTaskAssignment(evidence.getTask());
        validateTaskIsMutableByMember(evidence.getTask());
        
        Long currentMemberId = accessService.getCurrentMember().getId();
        if (evidence.getSubmittedByMemberId() == null || !evidence.getSubmittedByMemberId().equals(currentMemberId)) {
            throw new org.springframework.security.access.AccessDeniedException("Anggota tidak dapat menghapus bukti milik manager atau anggota lain");
        }

        evidence.setActive(false);
        evidenceRepository.save(evidence);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Bukti tugas milik anggota berhasil dinonaktifkan")
                .data(null)
                .build();
    }

    private void validateTaskIsMutableByMember(DivisionTask task) {
        if (!task.getActive()) {
            throw new BadRequestException("Tugas tidak aktif");
        }
        if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.CANCELLED) {
            throw new BadRequestException("Bukti pada tugas yang sudah selesai atau dibatalkan tidak dapat diubah oleh anggota");
        }
    }

    private void validateEvidenceRequest(DivisionTaskEvidenceRequest request) {
        EvidenceType type = defaultIfNull(request.getType(), EvidenceType.NOTE);
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Judul bukti wajib diisi");
        }

        if (type == EvidenceType.LINK) {
            if (request.getExternalLink() == null || request.getExternalLink().trim().isEmpty()) {
                throw new BadRequestException("External link wajib diisi untuk tipe LINK");
            }
        }

        if (type == EvidenceType.PHOTO || type == EvidenceType.DOCUMENT) {
            if (request.getFileUrl() == null || request.getFileUrl().trim().isEmpty()) {
                throw new BadRequestException("File URL wajib diisi untuk tipe PHOTO atau DOCUMENT");
            }
        }
        
        validateUrlSafe(request.getExternalLink(), true);
        validateUrlSafe(request.getFileUrl(), false);
    }

    private void validateUrlSafe(String url, boolean isExternalOnly) {
        if (url == null || url.trim().isEmpty()) return;
        
        String normalized = url.trim().toLowerCase(java.util.Locale.ROOT);
        
        if (normalized.startsWith("javascript:") || normalized.startsWith("data:")) {
            throw new BadRequestException("Format URL tidak valid");
        }
        
        if (isExternalOnly) {
            if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
                throw new BadRequestException("Format URL tidak valid, hanya http dan https yang diizinkan");
            }
        } else {
            if (!normalized.startsWith("http://") && !normalized.startsWith("https://") && !normalized.startsWith("/")) {
                throw new BadRequestException("Format URL tidak valid, harus berupa http, https, atau path relatif");
            }
        }
    }

    private DivisionTaskEvidence buildEvidenceFromRequest(DivisionTask task, DivisionTaskEvidenceRequest request) {
        return DivisionTaskEvidence.builder()
                .task(task)
                .type(defaultIfNull(request.getType(), EvidenceType.NOTE))
                .title(request.getTitle().trim())
                .description(trimOrNull(request.getDescription()))
                .fileUrl(trimOrNull(request.getFileUrl()))
                .externalLink(trimOrNull(request.getExternalLink()))
                .active(true)
                .build();
    }

    private void updateEvidenceFields(DivisionTaskEvidence evidence, DivisionTask task, DivisionTaskEvidenceRequest request) {
        evidence.setTask(task);
        evidence.setType(defaultIfNull(request.getType(), EvidenceType.NOTE));
        evidence.setTitle(request.getTitle().trim());
        evidence.setDescription(trimOrNull(request.getDescription()));
        evidence.setFileUrl(trimOrNull(request.getFileUrl()));
        evidence.setExternalLink(trimOrNull(request.getExternalLink()));
    }

    private DivisionTaskEvidence findEvidenceById(Long id) {
        DivisionTaskEvidence evidence = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bukti tugas divisi tidak ditemukan"));
        if (!evidence.getActive()) {
            throw new ResourceNotFoundException("Bukti tugas divisi tidak ditemukan");
        }
        return evidence;
    }

    private DivisionTask findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tugas divisi tidak ditemukan"));
    }

    private DivisionTaskEvidenceResponse mapToResponse(DivisionTaskEvidence evidence) {
        return DivisionTaskEvidenceResponse.builder()
                .id(evidence.getId())
                .taskId(evidence.getTask().getId())
                .taskTitle(evidence.getTask().getTitle())
                .type(evidence.getType())
                .title(evidence.getTitle())
                .description(evidence.getDescription())
                .fileUrl(evidence.getFileUrl())
                .externalLink(evidence.getExternalLink())
                .submittedByMemberId(evidence.getSubmittedByMemberId())
                .active(evidence.getActive())
                .createdAt(evidence.getCreatedAt())
                .updatedAt(evidence.getUpdatedAt())
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