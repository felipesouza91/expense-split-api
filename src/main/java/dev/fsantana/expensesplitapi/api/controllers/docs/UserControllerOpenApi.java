package dev.fsantana.expensesplitapi.api.controllers.docs;


import dev.fsantana.expensesplitapi.api.requests.CreateUserRequest;
import dev.fsantana.expensesplitapi.api.requests.SignInRequest;
import dev.fsantana.expensesplitapi.api.responses.ActivityListResponse;
import dev.fsantana.expensesplitapi.api.responses.SignUpResponse;
import dev.fsantana.expensesplitapi.api.responses.StatisticsResponse;
import dev.fsantana.expensesplitapi.api.responses.UserResponse;
import dev.fsantana.expensesplitapi.api.responses.UsersResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Expense")
@ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "500", description = "Server error",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
public interface UserControllerOpenApi {


    @Operation(summary = "Sign Up")
    @ApiResponse(responseCode = "201", description = "Sign Up data")
    @ApiResponse(responseCode = "400", description = "Email/password invalid",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<SignUpResponse> createUser(@Valid @RequestBody CreateUserRequest request);

    @Operation(summary = "Sign In")
    @ApiResponse(responseCode = "200", description = "Sign In data")
    @ApiResponse(responseCode = "400", description = "Email/password invalid",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<SignUpResponse> authenticateUser(@Valid @RequestBody SignInRequest request);

    @Operation(summary = "Get session user infos")
    @SecurityRequirement(name = "security_auth")
    @ApiResponse(responseCode = "200", description = "Session User data")
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<UserResponse> aboutUser();

    @Operation(summary = "Get all users")
    @SecurityRequirement(name = "security_auth")
    @ApiResponse(responseCode = "200", description = "Users data")
    public ResponseEntity<UsersResponse> findAll() ;

    @Operation(summary = "Get session user statistics")
    @SecurityRequirement(name = "security_auth")
    @ApiResponse(responseCode = "200", description = "Statistics data")
    public ResponseEntity<StatisticsResponse>  getStatistics();

    @Operation(summary = "Get session user activity")
    @SecurityRequirement(name = "security_auth")
    @ApiResponse(responseCode = "200", description = "User data")
    public ResponseEntity<ActivityListResponse> getUsersActivity();
}
