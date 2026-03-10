package dev.fsantana.expensesplitapi.api.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarkPaymentRequest {

    @NotNull
    @Min(1)
    private Long amountInCents;
}
