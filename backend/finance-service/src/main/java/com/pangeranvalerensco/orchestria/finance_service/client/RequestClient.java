package com.pangeranvalerensco.orchestria.finance_service.client;

import com.pangeranvalerensco.orchestria.finance_service.client.dto.FundRequestSnapshotResponse;
import com.pangeranvalerensco.orchestria.finance_service.exception.ExternalServiceException;
import com.pangeranvalerensco.orchestria.finance_service.exception.ForbiddenException;
import com.pangeranvalerensco.orchestria.finance_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.finance_service.payload.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class RequestClient {

    private final RestClient restClient;

    public RequestClient(
            @Value("${services.request.base-url:http://localhost:8003}")
            String requestServiceBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(requestServiceBaseUrl)
                .build();
    }

    public FundRequestSnapshotResponse getFundRequest(
            Long fundRequestId,
            String authorizationHeader
    ) {
        try {
            ApiResponse<FundRequestSnapshotResponse> response =
                    restClient.get()
                            .uri(
                                    "/api/requests/{id}",
                                    fundRequestId
                            )
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    authorizationHeader
                            )
                            .retrieve()
                            .body(
                                    new ParameterizedTypeReference<
                                            ApiResponse<FundRequestSnapshotResponse>
                                    >() {
                                    }
                            );

            if (response == null
                    || !Boolean.TRUE.equals(response.getSuccess())
                    || response.getData() == null) {
                throw new ExternalServiceException(
                        "Request-service mengembalikan response yang tidak valid"
                );
            }

            return response.getData();

        } catch (RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();

            if (statusCode == 404) {
                throw new ResourceNotFoundException(
                        "Pengajuan dana tidak ditemukan"
                );
            }

            if (statusCode == 401 || statusCode == 403) {
                throw new ForbiddenException(
                        "Token tidak memiliki akses untuk membaca pengajuan dana"
                );
            }

            throw new ExternalServiceException(
                    "Request-service sedang tidak dapat diakses"
            );

        } catch (RestClientException ex) {
            throw new ExternalServiceException(
                    "Request-service sedang tidak dapat diakses"
            );
        }
    }
}