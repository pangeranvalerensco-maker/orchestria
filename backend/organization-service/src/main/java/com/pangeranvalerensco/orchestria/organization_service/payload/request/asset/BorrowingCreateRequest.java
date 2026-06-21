package com.pangeranvalerensco.orchestria.organization_service.payload.request.asset;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record BorrowingCreateRequest(
        @NotBlank String assetId,
        @NotBlank @Size(max = 1000) String purpose,
        @NotNull @FutureOrPresent LocalDate borrowDate,
        @NotNull @FutureOrPresent LocalDate expectedReturnDate
) {}
