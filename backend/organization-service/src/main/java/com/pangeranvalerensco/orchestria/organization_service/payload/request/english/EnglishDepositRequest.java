package com.pangeranvalerensco.orchestria.organization_service.payload.request.english;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record EnglishDepositRequest(
        @NotBlank(message = "Activity ID wajib diisi")
        String activityId,

        @NotBlank(message = "Topik wajib diisi")
        String topic,

        @NotBlank(message = "Evidence URL wajib diisi")
        @Pattern(regexp = "^(http://|https://).*$", message = "Evidence URL wajib diawali http:// atau https://")
        String evidenceUrl,

        String submissionNote
) {
}
