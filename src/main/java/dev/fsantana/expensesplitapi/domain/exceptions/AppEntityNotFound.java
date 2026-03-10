package dev.fsantana.expensesplitapi.domain.exceptions;

public class AppEntityNotFound extends  RuntimeException{

    public AppEntityNotFound(String message) {
        super(message);
    }

}
