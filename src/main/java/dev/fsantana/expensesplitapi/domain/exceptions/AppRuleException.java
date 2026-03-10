package dev.fsantana.expensesplitapi.domain.exceptions;

public class AppRuleException extends  RuntimeException{

    public AppRuleException(String message) {
        super(message);
    }

}