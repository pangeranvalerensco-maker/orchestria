package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import com.pangeranvalerensco.orchestria.organization_service.entity.DivisionTask;
import com.pangeranvalerensco.orchestria.organization_service.entity.DivisionTaskEvidence;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EvidenceType;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionTaskEvidenceRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionTaskEvidenceResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionTaskEvidenceRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionTaskRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.DivisionTaskEvidenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DivisionTaskEvidenceServiceImpl implements DivisionTaskEvidenceService {

    private final DivisionTaskEvidenceRepository evidenceRepository;
    private final DivisionTaskRepository taskRepository;

    @Override
    public ApiResponse<List<DivisionTaskEvidenceResponse>> getEvidencesByTask(Long taskId) {
        DivisionTask task = findTaskById(taskId);

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

        return ApiResponse.<DivisionTaskEvidenceResponse>builder()
                .success(true)
                .message("Detail bukti tugas divisi berhasil diambil")
                .data(mapToResponse(evidence))
                .build();
    }

    @Override
    public ApiResponse<DivisionTaskEvidenceResponse> createEvidence(DivisionTaskEvidenceRequest request) {
        DivisionTask task = findTaskById(request.getTaskId());

        DivisionTaskEvidence evidence = DivisionTaskEvidence.builder()
                .task(task)
                .type(defaultIfNull(request.getType(), EvidenceType.NOTE))
                .title(request.getTitle().trim())
                .description(trimOrNull(request.getDescription()))
                .fileUrl(trimOrNull(request.getFileUrl()))
                .externalLink(trimOrNull(request.getExternalLink()))
                .active(true)
                .build();

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

        evidence.setTask(task);
        evidence.setType(defaultIfNull(request.getType(), EvidenceType.NOTE));
        evidence.setTitle(request.getTitle().trim());
        evidence.setDescription(trimOrNull(request.getDescription()));
        evidence.setFileUrl(trimOrNull(request.getFileUrl()));
        evidence.setExternalLink(trimOrNull(request.getExternalLink()));

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
        evidence.setActive(false);
        evidenceRepository.save(evidence);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Bukti tugas divisi berhasil dinonaktifkan")
                .data(null)
                .build();
    }

    private DivisionTaskEvidence findEvidenceById(Long id) {
        return evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bukti tugas divisi tidak ditemukan"));
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