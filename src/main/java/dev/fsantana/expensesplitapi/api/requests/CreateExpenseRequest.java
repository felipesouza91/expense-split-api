package dev.fsantana.expensesplitapi.api.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseRequest {

    @NotBlank
    private String title;
    @NotNull
    @Min(0)
    private Integer amountInCents;

    private UUID payerId;

    @NotNull
    @Size(min=1)
    private Set<UUID> participantsIds;
}
