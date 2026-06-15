package main.java.com.pangeranvalerensco.orchestria.finance_service.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}