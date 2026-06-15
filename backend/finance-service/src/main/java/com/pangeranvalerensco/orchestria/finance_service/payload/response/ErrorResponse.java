package main.java.com.pangeranvalerensco.orchestria.finance_service.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse<T> {

    private Boolean success;
    private String message;
    private T errors;
    private String path;
    private LocalDateTime timestamp;
}