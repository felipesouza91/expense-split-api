package dev.fsantana.expensesplitapi.infra.config.api.dto;

import lombok.Builder;

@Builder
public record FieldProblem(String name, String detail) {

}