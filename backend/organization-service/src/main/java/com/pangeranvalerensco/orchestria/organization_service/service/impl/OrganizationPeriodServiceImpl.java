package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pangeranvalerensco.orchestria.organization_service.entity.OrganizationPeriod;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.OrganizationPeriodRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.OrganizationPeriodResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.OrganizationPeriodRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.OrganizationPeriodService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationPeriodServiceImpl implements OrganizationPeriodService {

    private final OrganizationPeriodRepository periodRepository;

    @Override
    public ApiResponse<List<OrganizationPeriodResponse>> getAllPeriods() {
        List<OrganizationPeriodResponse> periods = periodRepository.findByActiveTrueOrderByStartDateDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<OrganizationPeriodResponse>>builder()
                .success(true)
                .message("Daftar Periode Berhasil diambil")
                .data(periods)
                .build();
    }

    @Override
    public ApiResponse<OrganizationPeriodResponse> getCurrentPeriod() {
        OrganizationPeriod period = periodRepository.findByCurrentPeriodTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Periode Aktif Tidak Ditemukan"));

        return ApiResponse.<OrganizationPeriodResponse>builder()
                .success(true)
                .message("Periode Aktif berhasil diambil")
                .data(mapToResponse(period))
                .build();
    }

    @Override
    public ApiResponse<OrganizationPeriodResponse> getPeriodById(Long id) {
        OrganizationPeriod period = findPeriodById(id);

        return ApiResponse.<OrganizationPeriodResponse>builder()
                .success(true)
                .message("Detail Periode Organisasi Berhasil diambil")
                .data(mapToResponse(period))
                .build();
    }

    @Override
    public ApiResponse<OrganizationPeriodResponse> createPeriod(OrganizationPeriodRequest request) {
        String name = request.getName().trim();

        if (periodRepository.existsByName(name)) {
            throw new BadRequestException("Nama Periode Sudah digunakan");
        }

        if (Boolean.TRUE.equals(request.getCurrentPeriod())) {
            deactivateOtherCurrentPeriods();
        }

        OrganizationPeriod period = OrganizationPeriod.builder()
                .name(name)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .currentPeriod(request.getCurrentPeriod())
                .publicVisible(request.getPublicVisible())
                .active(true)
                .build();

        OrganizationPeriod savedPeriod = periodRepository.save(period);

        return ApiResponse.<OrganizationPeriodResponse>builder()
                .success(true)
                .message("Periode Organisasi Berhasil dibuat")
                .data(mapToResponse(savedPeriod))
                .build();

    }

    @Override
    public ApiResponse<OrganizationPeriodResponse> updatePeriod(Long id, OrganizationPeriodRequest request) {
        OrganizationPeriod period = findPeriodById(id);
        String name = request.getName().trim();

        periodRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Nama periode sudah digunakan");
            }
        });

        if (Boolean.TRUE.equals(request.getCurrentPeriod())) {
            deactivateOtherCurrentPeriods();
        }

        period.setName(name);
        period.setStartDate(request.getStartDate());
        period.setEndDate(request.getEndDate());
        period.setCurrentPeriod(request.getCurrentPeriod());
        period.setPublicVisible(request.getPublicVisible());

        OrganizationPeriod savedPeriod = periodRepository.save(period);

        return ApiResponse.<OrganizationPeriodResponse>builder()
                .success(true)
                .message("Periode organisasi berhasil diperbarui")
                .data(mapToResponse(savedPeriod))
                .build();
    }


    @Override
    public ApiResponse<Void> deletePeriod(Long id) {
        OrganizationPeriod period = findPeriodById(id);

        if(Boolean.TRUE.equals(period.getCurrentPeriod())){
            throw new BadRequestException("Periode Aktif tidak boleh dinonaktifkan");
        }

        period.setActive(false);
        periodRepository.save(period);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Periode Organisasi berhasil dinonaktifkan")
                .data(null)
                .build();
    }

    private OrganizationPeriod findPeriodById(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Periode Organisasi tidak ditemukan"));
    }

    private void deactivateOtherCurrentPeriods() {
        periodRepository.findByCurrentPeriodTrue().ifPresent(existing -> {
            existing.setCurrentPeriod(false);
            periodRepository.save(existing);
        });
    }

    private OrganizationPeriodResponse mapToResponse(OrganizationPeriod period) {
        return OrganizationPeriodResponse.builder()
                .id(period.getId())
                .name(period.getName())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .currentPeriod(period.getCurrentPeriod())
                .publicVisible(period.getPublicVisible())
                .active(period.getActive())
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt())
                .build();
    }
}
