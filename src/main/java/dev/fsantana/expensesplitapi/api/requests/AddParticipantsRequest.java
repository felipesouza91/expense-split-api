package dev.fsantana.expensesplitapi.api.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class AddParticipantsRequest {
    @NotNull
    @Size(min =1)
    private Set<UUID> participantsIds;
}
