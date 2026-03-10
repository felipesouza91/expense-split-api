package dev.fsantana.expensesplitapi.domain.exceptions;

public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }

}