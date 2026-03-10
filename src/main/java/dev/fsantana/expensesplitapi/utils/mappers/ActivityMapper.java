package dev.fsantana.expensesplitapi.utils.mappers;

import dev.fsantana.expensesplitapi.api.requests.ActivityRequest;
import dev.fsantana.expensesplitapi.api.requests.AddParticipantsRequest;
import dev.fsantana.expensesplitapi.api.responses.ActivityBalanceResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityBalanceUserResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityParticipantsResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityResumeResponse;
import dev.fsantana.expensesplitapi.api.responses.ExpenseInfoParticipantResponse;
import dev.fsantana.expensesplitapi.api.responses.ExpenseInfoResponse;
import dev.fsantana.expensesplitapi.api.responses.ParticipantResume;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityBalance;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import dev.fsantana.expensesplitapi.domain.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper
public interface ActivityMapper {

    @Mapping(source = "title", target = "name")
    Activity toModel(ActivityRequest activityRequest);

    ActivityResumeResponse toResume(Activity saved);

    ActivityBalanceResponse toBalanceResponse(ActivityBalance balanceById);

    @Mapping(source = "id", target = "userId")
    ActivityBalanceUserResponse toActivityBalanceUserResponse(User user);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.email", target = "email")
    ParticipantResume toParticipantResume(ActivityParticipant participant);

    default  List<ActivityParticipant> fromAddParticipantsRequestToActivityParticipants(AddParticipantsRequest addParticipantsRequest ) {
       return  addParticipantsRequest.getParticipantsIds().stream().map( id -> {
            ActivityParticipant activityParticipant = new ActivityParticipant();
            User user = new User();
            user.setId(id);
            activityParticipant.setUser(user);
            return activityParticipant;
        }).toList();
    }

    default ActivityParticipantsResponse toActivityParticipantsResponse(List<ActivityParticipant> participants) {
        ActivityParticipantsResponse activityParticipantsResponse = new ActivityParticipantsResponse();
        Set<ParticipantResume> participantResumes = new HashSet<>();
        if (participants != null && !participants.isEmpty()) {
            ActivityParticipant activityParticipant = participants.stream().findFirst().get();
            activityParticipantsResponse.setActivityId(activityParticipant.getActivity().getId());
            activityParticipantsResponse.setActivityName(activityParticipant.getActivity().getName());
            participants.forEach(item -> {
                participantResumes.add(this.toParticipantResume(item));
            });
            activityParticipantsResponse.setParticipants(participantResumes);
        }
        return activityParticipantsResponse;
    }

    default ActivityResponse toActivityResponse(Activity activity, User user) {
        List<ParticipantResume> participantResume = activity.getParticipants().stream()
                .map(activityParticipant -> ParticipantResume.builder()
                        .id(activityParticipant.getUser().getId())
                        .name(activityParticipant.getUser().getName())
                        .email(activityParticipant.getUser().getEmail())
                        .joinedAt(activityParticipant.getJoinedAt())
                        .build())
                .toList();

        List<ExpenseInfoResponse> expenseInfos = new ArrayList<>();
        for (Expense expense : activity.getExpenses()) {

            Integer fullyPaidCount = 0;
            boolean hasPartialPayment = false;
            List<ExpenseInfoParticipantResponse> expenseParticipantsInfo = new ArrayList<>();

            for (ExpenseParticipant ep: expense.getExpenseParticipants()) {
                Long totalPaid = ep.getExpense().getExpensePayments().stream()
                        .filter(expensePayment -> expensePayment.getExpense().getId().equals(expense.getId())
                                && expensePayment.getDebtor().getId().equals(user.getId()))
                        .reduce(0L, (total, data) -> total + data.getAmountPaidInCents(), Long::sum);
                String participantPaymentStatus = "";
                if (totalPaid >= ep.getAmountOwedInCents()) {
                    participantPaymentStatus = "paid";
                    fullyPaidCount += 1;
                } else if ( totalPaid > 0) {
                    participantPaymentStatus = "partial";
                    hasPartialPayment = true;
                } else {
                    participantPaymentStatus = "pending";
                }
                expenseParticipantsInfo.add(
                        ExpenseInfoParticipantResponse.builder()
                                .id(ep.getUser().getId())
                                .name(ep.getUser().getName())
                                .email(ep.getUser().getEmail())
                                .paymentStatus(participantPaymentStatus)
                                .build()
                );
            }

            String paymentStatus;
            if (fullyPaidCount == 0 && !hasPartialPayment) {
                paymentStatus = "pending";
            } else if ( fullyPaidCount == expense.getExpenseParticipants().size() ){
                paymentStatus = "paid";
            } else {
                paymentStatus = "partial";
            }

            expenseInfos.add(ExpenseInfoResponse.builder()
                    .id(expense.getId())
                    .name(expense.getName())
                    .amountInCents(expense.getAmountInCents())
                    .payerName(expense.getPayer().getName())
                    .payerId(expense.getPayer().getId())
                    .participants(expenseParticipantsInfo)
                    .paymentStatus(paymentStatus)
                    .build()
            );

        }

        Long totalAmount = activity.getExpenses().stream()
                .reduce(0L, (total, expense) -> total + expense.getAmountInCents(), Long::sum);

        return ActivityResponse.builder()
                .id(activity.getId())
                .name(activity.getName())
                .activityDate(activity.getActivityDate())
                .participants(participantResume)
                .expenses(expenseInfos)
                .totalAmountInCents(totalAmount)
                .build();
    }
}
