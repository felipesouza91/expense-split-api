package dev.fsantana.expensesplitapi.domain.exceptions;

public class AppSecurityException extends AppException {
    public AppSecurityException(String message) {
        super(message);
    }
}