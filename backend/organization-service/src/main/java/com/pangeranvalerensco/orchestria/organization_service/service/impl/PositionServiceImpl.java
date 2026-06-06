package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pangeranvalerensco.orchestria.organization_service.entity.Position;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.PositionRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PositionResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.PositionRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.PositionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    @Override
    public ApiResponse<List<PositionResponse>> getAllPositions() {
        List<PositionResponse> positions = positionRepository.findByActiveTrueOrderByLevelOrderAscNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<PositionResponse>>builder()
                .success(true)
                .message("Daftar Jabatan berhasil diambil")
                .data(positions)
                .build();
    }

    @Override
    public ApiResponse<PositionResponse> getPositionById(Long id) {
        Position position = findPositionById(id);

        return ApiResponse.<PositionResponse>builder()
                .success(true)
                .message("Detail Jabatan berhasil diambil")
                .data(mapToResponse(position))
                .build();
    }

    @Override
    public ApiResponse<PositionResponse> createPosition(PositionRequest request) {
        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        if (positionRepository.existsByCode(code)) {
            throw new RuntimeException("Kode Jabatan sudah digunakan");
        }

        if (positionRepository.existsByName(name)) {
            throw new RuntimeException("Nama Jabatan sudah digunakan");
        }

        Position position = Position.builder()
                .code(code)
                .name(name)
                .description(request.getDescription())
                .levelOrder(request.getLevelOrder())
                .publicVisible(request.getPublicVisible())
                .active(true)
                .build();

        Position savedPosition = positionRepository.save(position);

        return ApiResponse.<PositionResponse>builder()
                .success(true)
                .message("Jabatan berhasil dibuat")
                .data(mapToResponse(savedPosition))
                .build();
    }

    @Override
    public ApiResponse<PositionResponse> updatePosition(Long id, PositionRequest request) {
        Position position = findPositionById(id);

        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        positionRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Kode Jabatan sudah digunakan");
            }
        });

        positionRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new RuntimeException("Nama Jabatan sudah digunakan");
            }
        });

        position.setCode(code);
        position.setName(name);
        position.setDescription(request.getDescription());
        position.setLevelOrder(request.getLevelOrder());
        position.setPublicVisible(request.getPublicVisible());

        Position savedPosition = positionRepository.save(position);

        return ApiResponse.<PositionResponse>builder()
                .success(true)
                .message("Jabatan berhasil diperbarui")
                .data(mapToResponse(savedPosition))
                .build();
    }

    @Override
    public ApiResponse<Void> deletePosition(Long id) {
        Position position = findPositionById(id);
        position.setActive(false);
        positionRepository.save(position);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Jabatan berhasil dihapus")
                .data(null)
                .build();
    }

    private Position findPositionById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jabatan dengan ID " + id + " tidak ditemukan"));
    }

    private PositionResponse mapToResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .code(position.getCode())
                .name(position.getName())
                .description(position.getDescription())
                .levelOrder(position.getLevelOrder())
                .publicVisible(position.getPublicVisible())
                .active(position.getActive())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }
    
}
