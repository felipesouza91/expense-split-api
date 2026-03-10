package dev.fsantana.expensesplitapi.utils.mappers;

import dev.fsantana.expensesplitapi.api.requests.CreateUserRequest;
import dev.fsantana.expensesplitapi.api.responses.ActivityListResponse;
import dev.fsantana.expensesplitapi.api.responses.ActivityUserResponse;
import dev.fsantana.expensesplitapi.api.responses.ParticipantResume;
import dev.fsantana.expensesplitapi.api.responses.SignUpResponse;
import dev.fsantana.expensesplitapi.api.responses.StatisticsResponse;
import dev.fsantana.expensesplitapi.api.responses.UserInfoResume;
import dev.fsantana.expensesplitapi.api.responses.UserResponse;
import dev.fsantana.expensesplitapi.api.responses.UsersResponse;
import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.Statistics;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.security.models.Auth;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper
public interface UserMapper {

    @Mapping(source = "password", target = "passwordHash")
    User toModel(CreateUserRequest request);


    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.email", target = "email")
    SignUpResponse toDTO(Auth auth);

    UserResponse toUserResponseDTO(User user);

    StatisticsResponse toStatisticsDTO(Statistics statistics);

    ParticipantResume toParticipantResumeDTO(User user);

    @Mapping(source = "id", target = "userId")
    UserInfoResume toUserInfoResume(User user);

    default UsersResponse toUsersResponse(Set<User> users) {
        UsersResponse usersResponse = new UsersResponse();
        usersResponse.setUsers(users.stream().map(this::toUserResponseDTO).collect(Collectors.toSet()));
        return usersResponse;
    }

   default ActivityListResponse toActivityListResponse(Set<ActivityParticipant> participants) {
       List<ActivityUserResponse> activityItems = participants.stream()
               .map(participant -> {
                   Activity activity = participant.getActivity();

                   // Calculate total using Streams (like Swift's reduce)
                   long totalAmount = activity.getExpenses().stream()
                           .mapToLong(Expense::getAmountInCents)
                           .sum();

                   Set<ParticipantResume> participantInfos = activity.getParticipants().stream()
                           .map(p -> new ParticipantResume(p.getUser().getId(), p.getUser().getName(), p.getUser().getEmail(), null))
                           .collect(Collectors.toSet());

                   return new ActivityUserResponse(
                           activity.getId(),
                           activity.getName(),
                           totalAmount,
                           activity.getActivityDate(),
                           activity.getParticipants().size(),
                           participantInfos,
                           activity.getExpenses().size()
                   );
               })
               .collect(Collectors.toList());
       return  new ActivityListResponse(activityItems);
   }
}
