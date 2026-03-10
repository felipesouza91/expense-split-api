package dev.fsantana.expensesplitapi.api.controllers;

import dev.fsantana.expensesplitapi.api.responses.BalanceBetweenUserResponse;
import dev.fsantana.expensesplitapi.api.responses.UserGlobalBalanceResponse;
import dev.fsantana.expensesplitapi.api.responses.balances.DetailedBalanceResponse;
import dev.fsantana.expensesplitapi.domain.services.BalanceService;
import dev.fsantana.expensesplitapi.domain.services.dto.BalanceBetweenUser;
import dev.fsantana.expensesplitapi.domain.services.dto.DetailedBalance;
import dev.fsantana.expensesplitapi.domain.services.dto.UserGlobalBalance;
import dev.fsantana.expensesplitapi.utils.mappers.BalanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;
    private final BalanceMapper balanceMapper;

    @GetMapping("/between/{userIdFrom}/{userIdTo}")
    public ResponseEntity<BalanceBetweenUserResponse> getBalanceBetween(@PathVariable UUID userIdFrom, @PathVariable UUID userIdTo) {
        BalanceBetweenUser balanceBetweenUser = balanceService.balanceBetweenUser(userIdFrom, userIdTo);
        BalanceBetweenUserResponse balanceResponse = balanceMapper.toBalanceResponse(balanceBetweenUser);
        return ResponseEntity.ok(balanceResponse);
    }

    @GetMapping("/users/{userId}/global")
    public ResponseEntity<UserGlobalBalanceResponse> getUserGlobalBalance(@PathVariable UUID userId) {
        UserGlobalBalance balance = balanceService.loadGlobalBalanceByUserId(userId);
        UserGlobalBalanceResponse response = balanceMapper.toUserGlobalBalanceResponse(balance);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/detailed")
    public ResponseEntity<DetailedBalanceResponse> getDetailedBalanceByUser(@PathVariable UUID userId) {
        DetailedBalance detailedBalanceByUser = balanceService.getDetailedBalanceByUser(userId);
        return ResponseEntity.ok(balanceMapper.toDetailedBalanceResponse(detailedBalanceByUser));
    }
}
