package dev.fsantana.expensesplitapi.api.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequest {

    @NotBlank
    @Size(min = 3)
    private String title;
    @NotNull
    private OffsetDateTime activityDate;
}
