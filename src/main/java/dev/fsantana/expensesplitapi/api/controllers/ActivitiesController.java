package dev.fsantana.expensesplitapi.api.controllers;

import dev.fsantana.expensesplitapi.api.requests.ActivityRequest;
import dev.fsantana.expensesplitapi.api.requests.AddParticipantsRequest;
import dev.fsantana.expensesplitapi.api.responses.ActivityBalanceResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityParticipantsResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityResumeResponse;
import dev.fsantana.expensesplitapi.api.responses.AddParticipantsResponse;
import dev.fsantana.expensesplitapi.api.responses.ExpenseListItemResponse;
import dev.fsantana.expensesplitapi.api.responses.ExpenseListResponse;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityBalance;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.services.ActivityService;
import dev.fsantana.expensesplitapi.domain.services.ExpenseService;
import dev.fsantana.expensesplitapi.domain.services.dto.AddParticipants;
import dev.fsantana.expensesplitapi.security.services.UserSessionService;
import dev.fsantana.expensesplitapi.utils.mappers.ActivityMapper;
import dev.fsantana.expensesplitapi.utils.mappers.ExpenseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivitiesController {

    private final ActivityService activityService;
    private final UserSessionService userSessionService;
    private final ActivityMapper activityMapper;
    private final ExpenseMapper expenseMapper;
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ActivityResumeResponse> createActivity(@Valid  @RequestBody ActivityRequest request) {
        Activity activity = activityMapper.toModel(request);
        Activity saved = activityService.save(activity);
        return ResponseEntity.status(HttpStatus.CREATED).body(activityMapper.toResume(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResumeResponse> updateActivity(@PathVariable UUID id, @Valid @RequestBody ActivityRequest request) {
        Activity activity = activityMapper.toModel(request);
        Activity updated = activityService.update(id, activity);
        return ResponseEntity.ok(activityMapper.toResume(updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getById(@PathVariable UUID id) {
        User user = userSessionService.getCurrentUser();
        Activity activity = activityService.findById(id);
        ActivityResponse result = activityMapper.toActivityResponse(activity, user);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        activityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{activityId}/balance")
    public ResponseEntity<ActivityBalanceResponse> getBalance(@PathVariable UUID activityId) {
        ActivityBalance balanceById = activityService.getBalanceByActivityId(activityId);
        ActivityBalanceResponse balanceResponse = activityMapper.toBalanceResponse(balanceById);
        return ResponseEntity.ok(balanceResponse);
    }

    @PostMapping("/{activityId}/participants")
    public ResponseEntity<AddParticipantsResponse> addParticipants(@PathVariable UUID activityId, @Valid @RequestBody AddParticipantsRequest addParticipantsRequest) {
        List<ActivityParticipant> list = activityMapper.fromAddParticipantsRequestToActivityParticipants(addParticipantsRequest);
        AddParticipants activity = activityService.addParticipants(activityId, list );
        AddParticipantsResponse expenseListItemResponses = expenseMapper.fromAddParticipantstoAddParticipantsResponse(activity);
        return  ResponseEntity.ok(expenseListItemResponses);
    }

    @GetMapping("/{activityId}/participants")
    public ResponseEntity<ActivityParticipantsResponse> listParticipants(@PathVariable UUID activityId) {
        List<ActivityParticipant> participants = activityService.findParticipantsByActivityId(activityId);
        ActivityParticipantsResponse activityParticipantsResponse = activityMapper.toActivityParticipantsResponse(participants);
        return ResponseEntity.ok(activityParticipantsResponse);
    }

    @GetMapping("/{activityId}/expenses")
    public ResponseEntity<ExpenseListResponse> getExpensesByActivityId(@PathVariable UUID activityId) {
        List<Expense> expenses = expenseService.listByActivityId(activityId);
        List<ExpenseListItemResponse> expenseListItemResponses = expenseMapper.toExpenseListItem(expenses);
        return  ResponseEntity.ok(new ExpenseListResponse(expenseListItemResponses));
    }

    @DeleteMapping("/{activityId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipant(@PathVariable UUID activityId, @PathVariable UUID participantId) {
        activityService.deleteParticipantByActivityId(activityId, participantId);
        return ResponseEntity.noContent().build();
    }
}
