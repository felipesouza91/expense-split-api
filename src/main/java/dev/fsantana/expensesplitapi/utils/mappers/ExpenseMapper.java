package dev.fsantana.expensesplitapi.utils.mappers;

import dev.fsantana.expensesplitapi.api.requests.CreateExpenseRequest;
import dev.fsantana.expensesplitapi.api.requests.UpdateExpenseRequest;
import dev.fsantana.expensesplitapi.api.requests.UpdatePayerExpenseRequest;
import dev.fsantana.expensesplitapi.api.responses.AddParticipantsResponse;
import dev.fsantana.expensesplitapi.api.responses.CreateExpenseResponse;
import dev.fsantana.expensesplitapi.api.responses.ExpenseDetailResponse;
import dev.fsantana.expensesplitapi.api.responses.ExpenseListItemResponse;
import dev.fsantana.expensesplitapi.api.responses.ParticipantDebt;
import dev.fsantana.expensesplitapi.api.responses.ParticipantInfoResponse;
import dev.fsantana.expensesplitapi.api.responses.ParticipantResume;
import dev.fsantana.expensesplitapi.api.responses.PayerInfoResponse;
import dev.fsantana.expensesplitapi.api.responses.PaymentInfoResponse;
import dev.fsantana.expensesplitapi.api.responses.ToggleParticipantPaymentResponse;
import dev.fsantana.expensesplitapi.api.responses.UpdateExpensePaymentResponse;
import dev.fsantana.expensesplitapi.api.responses.UpdatePayerExpanseResponse;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import dev.fsantana.expensesplitapi.domain.models.ExpensePayment;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.services.dto.AddParticipants;
import dev.fsantana.expensesplitapi.domain.services.dto.ToggleParticipantPayment;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mapper()
public interface ExpenseMapper {

    @Mapping(source = "title", target = "name")
    @Mapping(source = "payerId", target = "payer.id")
    Expense toModel(CreateExpenseRequest request);

    @Mapping(source = "request.title", target = "name")
    @Mapping(source = "request.payerId", target = "payer.id")
    @Mapping(source = "request.participantsIds", target = "expenseParticipants")
    @Mapping(source = "activityId", target = "activity.id")
    Expense toModel(CreateExpenseRequest request, UUID activityId);

    @Mapping(source = "request.title", target = "name")
    @Mapping(source = "request.payerId", target = "payer.id")
    @Mapping(source = "request.participantsIds", target = "expenseParticipants")
    @Mapping(source = "activityId", target = "activity.id")
    Expense fromUpdateToModel(UpdateExpenseRequest request, UUID activityId);

    default Set<ExpenseParticipant> fromUUID(Set<UUID> uuid) {
        Set<ExpenseParticipant> values = new HashSet<>();
        if (uuid != null) {
            uuid.forEach(id -> {
                ExpenseParticipant expenseParticipant = new ExpenseParticipant();
                User user = new User();
                user.setId(id);
                expenseParticipant.setUser(user);
                values.add(expenseParticipant) ;
            });
        }
        return values;
    }

    @Mapping(source = "payer.id", target = "payerId")
    @Mapping(source = "payer.name", target = "payerName")
    @Mapping(source = "activity.id", target = "activityId")
    @Mapping(source = "expenseParticipants", target = "participants")
    CreateExpenseResponse toCreateResponse(Expense save);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    ParticipantDebt toResume(ExpenseParticipant expenseParticipant);

    List<ExpenseListItemResponse> toExpenseListItem(List<Expense> expenses);

    @Mapping(expression = "java(expense.getExpenseParticipants().size())", target = "participantsCount")
    ExpenseListItemResponse toExpenseListItemResponse(Expense expense);

    @Mapping(source = "id", target = "userId")
    PayerInfoResponse toPayerInfoResponse(User user);


    @Mapping(source = "request.payerId", target = "payer.id")
    @Mapping(source = "expenseId", target = "id")
    Expense fromPayerUpdateToModel(@Valid UpdatePayerExpenseRequest request, UUID expenseId);

    @Mapping(source = "payer.id", target = "payerId")
    @Mapping(source = "payer.name", target = "payerName")
    UpdatePayerExpanseResponse fromModelToUpdatePayerExpanseResponse(Expense expense);

    @Mapping(source = "expense.id", target = "expenseId")
    @Mapping(source = "debtor.id", target = "debtorId")
    @Mapping(source = "debtor.name", target = "debtorName")
    UpdateExpensePaymentResponse toExpensePaymentResponse(ExpensePayment update);

    @Mapping(source = "expense.id", target = "expenseId")
    @Mapping(source = "participant.user.id", target = "participantId")
    @Mapping(source = "participant.user.name", target = "participantName")
    @Mapping(source = "participant.user.email", target = "participantEmail")
    ToggleParticipantPaymentResponse toToggleParticipantPaymentResponse(ToggleParticipantPayment toggleParticipantPayment);

    @Mapping(source = "id", target = "activityId")
    @Mapping(source = "participants", target = "addedParticipants")
    AddParticipantsResponse toAddParticipantsResponse(Activity activity);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.email", target = "email")
    ParticipantResume fromModelToParticipantResume(ActivityParticipant activityParticipant);

    @Mapping(source = "activity.id", target = "activityId")
    @Mapping(source = "activity.participants", target = "addedParticipants")
    AddParticipantsResponse fromAddParticipantstoAddParticipantsResponse(AddParticipants activity);


    default ExpenseDetailResponse toExpenseDetailResponse(Expense expense){
        Map<UUID, Long> paymentsByParticipant = new HashMap<>();
        for (ExpensePayment expensePayment : expense.getExpensePayments()) {
            Long total = paymentsByParticipant.get(expensePayment.getDebtor().getId());
            Long totalNew = total == null ? 0 : total;
            paymentsByParticipant.put(expensePayment.getDebtor().getId(), totalNew + expensePayment.getAmountPaidInCents());
        }
        List<ParticipantInfoResponse> participantsInfos = new ArrayList<>();

        for(ExpenseParticipant ep: expense.getExpenseParticipants()){
            Long amountPaid = paymentsByParticipant.getOrDefault(ep.getUser().getId(), 0L);
            Long remainingDebt = Math.max(0, ep.getAmountOwedInCents() - amountPaid);
            String paymentStatus ;
            if (amountPaid == 0) {
                paymentStatus = "pending";
            } else if( remainingDebt == 0) {
                paymentStatus = "paid";
            } else {
                paymentStatus = "partial";
            }

            participantsInfos.add(ParticipantInfoResponse.builder()
                    .userId(ep.getUser().getId())
                    .name(ep.getUser().getName())
                    .email(ep.getUser().getEmail())
                    .amountOwedInCents(ep.getAmountOwedInCents())
                    .amountPaidInCents(amountPaid).remainingDebtInCents(remainingDebt).paymentStatus(paymentStatus)
                    .build());
        }
        PayerInfoResponse payerInfoResponse = new PayerInfoResponse(expense.getPayer().getId(), expense.getPayer().getName(), expense.getPayer().getEmail());

        List<PaymentInfoResponse> paymentInfos = expense.getExpensePayments().stream()
                .map(item -> PaymentInfoResponse.builder()
                        .id(item.getId())
                        .debtorId(item.getDebtor().getId())
                        .debtorName(item.getDebtor().getName())
                        .amountPaidInCents(item.getAmountPaidInCents())
                        .paidAt(item.getPaidAt()).build()).toList();
        return ExpenseDetailResponse.builder()
                .id(expense.getId())
                .name(expense.getName())
                .amountInCents(expense.getAmountInCents())
                .payer(payerInfoResponse)
                .activityId(expense.getActivity().getId())
                .activityName(expense.getActivity().getName())
                .participants(participantsInfos).payments(paymentInfos).createdAt(expense.getCreatedAt())
                .build();
    }
}
