package dev.fsantana.expensesplitapi.domain.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceBetweenUser {

    private NetBalance netBalance;
    private List<ActivityDetail> details = new ArrayList<>();
}
