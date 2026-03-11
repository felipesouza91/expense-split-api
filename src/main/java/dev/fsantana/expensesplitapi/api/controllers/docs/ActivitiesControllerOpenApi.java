package dev.fsantana.expensesplitapi.api.controllers.docs;

import dev.fsantana.expensesplitapi.api.requests.ActivityRequest;
import dev.fsantana.expensesplitapi.api.requests.AddParticipantsRequest;
import dev.fsantana.expensesplitapi.api.responses.ActivityBalanceResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityParticipantsResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityResumeResponse;
import dev.fsantana.expensesplitapi.api.responses.AddParticipantsResponse;
import dev.fsantana.expensesplitapi.api.responses.ExpenseListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Activities")
@SecurityRequirement(name = "security_auth")
@ApiResponse(responseCode = "401", description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "500", description = "Server error",
        content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
public interface ActivitiesControllerOpenApi {

    @Operation(summary = "Create activity")
    @ApiResponse(responseCode = "201", description = "Activity data")
    public ResponseEntity<ActivityResumeResponse> createActivity(@Valid @RequestBody ActivityRequest request);

    @Operation(summary = "Update activity")
    @ApiResponse(responseCode = "200", description = "Activity data")
    @ApiResponse(responseCode = "404", description = "Activity not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ActivityResumeResponse> updateActivity(
            @PathVariable UUID id,
            @Valid @RequestBody ActivityRequest request);

    @Operation(summary = "Find activity by id")
    @ApiResponse(responseCode = "200", description = "Activity data")
    @ApiResponse(responseCode = "404", description = "Activity not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ActivityResponse> getById(@PathVariable UUID id) ;

    @Operation(summary = "Delete activity by id")
    @ApiResponse(responseCode = "204", description = "Delete success")
    @ApiResponse(responseCode = "404", description = "Activity not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> deleteById(@PathVariable UUID id);

    @Operation(summary = "Load activity balance")
    @ApiResponse(responseCode = "200", description = "Activity balance")
    @ApiResponse(responseCode = "404", description = "Activity not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ActivityBalanceResponse> getBalance(@PathVariable UUID activityId) ;

    @Operation(summary = "Add participants to a activity")
    @ApiResponse(responseCode = "200", description = "Added participant data")
    @ApiResponse(responseCode = "400", description = "Participant not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Activity not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<AddParticipantsResponse> addParticipants(
            @PathVariable UUID activityId,
            @Valid @RequestBody AddParticipantsRequest addParticipantsRequest);

    @Operation(summary = "Get participants to a activity")
    @ApiResponse(responseCode = "200", description = "Activity participants data")
    @ApiResponse(responseCode = "404", description = "Activity not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ActivityParticipantsResponse> listParticipants(@PathVariable UUID activityId) ;

    @Operation(summary = "Get expenses to a activity")
    @ApiResponse(responseCode = "200", description = "Expense data")
    @ApiResponse(responseCode = "400", description = "Session user is not participant of this activity",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Activity not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ExpenseListResponse> getExpensesByActivityId(@PathVariable UUID activityId) ;

    @Operation(summary = "Remove activity participant")
    @ApiResponse(responseCode = "204", description = "Deleted success")
    @ApiResponse(responseCode = "400", description = "Activity not found or Session user not is a participant",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Activity not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> removeParticipant(@PathVariable UUID activityId, @PathVariable UUID participantId);
}
