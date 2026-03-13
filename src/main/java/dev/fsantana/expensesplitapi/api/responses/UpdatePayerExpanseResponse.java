package dev.fsantana.expensesplitapi.api.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePayerExpanseResponse {

    private UUID id;
    private String name;
    private UUID payerId;
    private String payerName;
    private OffsetDateTime updatedAt;
}
