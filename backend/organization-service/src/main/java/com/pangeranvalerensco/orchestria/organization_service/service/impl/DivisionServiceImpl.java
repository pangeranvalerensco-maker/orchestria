package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pangeranvalerensco.orchestria.organization_service.entity.Division;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.DivisionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;

    @Override
    public ApiResponse<List<DivisionResponse>> getAllDivisions() {
        List<DivisionResponse> divisions = divisionRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<DivisionResponse>>builder()
                .success(true)
                .message("Daftar Divisi berhasil diambil")
                .data(divisions)
                .build();
    }

    @Override
    public ApiResponse<DivisionResponse> getDivisionById(Long id) {
        DivisionResponse division = divisionRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Divisi dengan ID " + id + " tidak ditemukan"));

        return ApiResponse.<DivisionResponse>builder()
                .success(true)
                .message("Detail Divisi berhasil diambil")
                .data(division)
                .build();
    }

    @Override
    public ApiResponse<DivisionResponse> createDivision(DivisionRequest request) {
        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        if (divisionRepository.existsByCode(code)) {
            throw new BadRequestException("Kode Divisi sudah digunakan");
        }

        if (divisionRepository.existsByName(name)) {
            throw new BadRequestException("Nama Divisi sudah digunakan");
        }

        Division division = Division.builder()
                .code(code)
                .name(name)
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .publicVisible(request.getPublicVisible())
                .active(true)
                .build();

        Division savedDivision = divisionRepository.save(division);

        return ApiResponse.<DivisionResponse>builder()
                .success(true)
                .message("Divisi berhasil dibuat")
                .data(mapToResponse(savedDivision))
                .build();
    }

    @Override
    public ApiResponse<DivisionResponse> updateDivision(Long id, DivisionRequest request) {
        Division division = findDivisionById(id);

        String code = request.getCode().trim().toUpperCase();
        String name = request.getName().trim();

        divisionRepository.findByCode(code).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Kode Divisi sudah digunakan");
            }
        });

        divisionRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Nama Divisi sudah digunakan");
            }
        });

        division.setCode(code);
        division.setName(name);
        division.setDescription(request.getDescription());
        division.setDisplayOrder(request.getDisplayOrder());
        division.setPublicVisible(request.getPublicVisible());

        Division savedDivision = divisionRepository.save(division);

        return ApiResponse.<DivisionResponse>builder()
                .success(true)
                .message("Divisi berhasil diperbarui")
                .data(mapToResponse(savedDivision))
                .build();
    }

    @Override
    public ApiResponse<Void> deleteDivision(Long id) {
        Division division = findDivisionById(id);
        division.setActive(false);
        divisionRepository.save(division);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Divisi berhasil dinonaktifkan")
                .build();
    }

    private Division findDivisionById(Long id) {
        return divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Divisi dengan ID " + id + " tidak ditemukan"));
    }

    private DivisionResponse mapToResponse(Division division) {
        return DivisionResponse.builder()
                .id(division.getId())
                .code(division.getCode())
                .name(division.getName())
                .description(division.getDescription())
                .displayOrder(division.getDisplayOrder())
                .publicVisible(division.getPublicVisible())
                .active(division.getActive())
                .createdAt(division.getCreatedAt())
                .updatedAt(division.getUpdatedAt())
                .build();
    }
}
