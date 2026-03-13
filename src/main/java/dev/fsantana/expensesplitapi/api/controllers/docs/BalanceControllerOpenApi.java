package dev.fsantana.expensesplitapi.api.controllers.docs;

import dev.fsantana.expensesplitapi.api.responses.BalanceBetweenUserResponse;
import dev.fsantana.expensesplitapi.api.responses.UserGlobalBalanceResponse;
import dev.fsantana.expensesplitapi.api.responses.balances.DetailedBalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "Balance")
@SecurityRequirement(name = "security_auth")
@ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "500", description = "Server error",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
public interface BalanceControllerOpenApi {

    @Operation(summary = "Get balance between users")
    @ApiResponse(responseCode = "200", description = "Balance data")
    @ApiResponse(responseCode = "400", description = "Invalid user",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<BalanceBetweenUserResponse> getBalanceBetween(
            @PathVariable UUID userIdFrom,
            @PathVariable UUID userIdTo) ;

    @Operation(summary = "Get global users balance  ")
    @ApiResponse(responseCode = "200", description = "Global Balance data")
    @ApiResponse(responseCode = "400", description = "User balance request is not the current user",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<UserGlobalBalanceResponse> getUserGlobalBalance(@PathVariable UUID userId) ;

    @Operation(summary = "Get detailed users balance  ")
    @ApiResponse(responseCode = "200", description = "Detailed Balance data")
    @ApiResponse(responseCode = "400", description = "User balance request is not the current user",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<DetailedBalanceResponse> getDetailedBalanceByUser(@PathVariable UUID userId);
}
