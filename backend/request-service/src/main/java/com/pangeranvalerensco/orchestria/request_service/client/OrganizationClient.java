package com.pangeranvalerensco.orchestria.request_service.client;

import com.pangeranvalerensco.orchestria.request_service.client.dto.OrganizationMemberContextResponse;
import com.pangeranvalerensco.orchestria.request_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.request_service.exception.ExternalServiceException;
import com.pangeranvalerensco.orchestria.request_service.exception.ForbiddenException;
import com.pangeranvalerensco.orchestria.request_service.payload.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

@Component
public class OrganizationClient {

    private final RestClient restClient;

    public OrganizationClient(
            @Value("${services.organization.base-url:http://localhost:8002}")
            String organizationBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(organizationBaseUrl)
                .build();
    }

    public OrganizationMemberContextResponse getCurrentMemberContext(
            String authorizationHeader
    ) {
        try {
            ApiResponse<OrganizationMemberContextResponse> response =
                    restClient.get()
                            .uri("/api/organization/members/me/context")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorizationHeader
                            )
                            .retrieve()
                            .body(
                                    new ParameterizedTypeReference<
                                            ApiResponse<OrganizationMemberContextResponse>
                                    >() {
                                    }
                            );

            if (response == null
                    || !Boolean.TRUE.equals(response.getSuccess())
                    || response.getData() == null) {
                throw new ExternalServiceException(
                        "Organization-service mengembalikan response yang tidak valid"
                );
            }

            return response.getData();

        } catch (RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();

            if (statusCode == 404) {
                throw new BadRequestException(
                        "Akun login belum terhubung dengan data anggota organisasi"
                );
            }

            if (statusCode == 401 || statusCode == 403) {
                throw new ForbiddenException(
                        "Token tidak dapat digunakan untuk membaca data organisasi"
                );
            }

            throw new ExternalServiceException(
                    "Organization-service sedang tidak dapat diakses"
            );
        }
    }
}