package dev.fsantana.expensesplitapi.api.controllers;

import dev.fsantana.expensesplitapi.api.controllers.docs.UserControllerOpenApi;
import dev.fsantana.expensesplitapi.api.requests.CreateUserRequest;
import dev.fsantana.expensesplitapi.api.requests.SignInRequest;
import dev.fsantana.expensesplitapi.api.responses.ActivityListResponse;
import dev.fsantana.expensesplitapi.api.responses.SignUpResponse;
import dev.fsantana.expensesplitapi.api.responses.StatisticsResponse;
import dev.fsantana.expensesplitapi.api.responses.UserResponse;
import dev.fsantana.expensesplitapi.api.responses.UsersResponse;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Statistics;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.services.ActivityService;
import dev.fsantana.expensesplitapi.domain.services.UserService;
import dev.fsantana.expensesplitapi.security.models.Auth;
import dev.fsantana.expensesplitapi.security.services.SessionService;
import dev.fsantana.expensesplitapi.security.services.UserSessionService;
import dev.fsantana.expensesplitapi.utils.mappers.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController implements UserControllerOpenApi {

    private final SessionService sessionService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserSessionService userSessionService;
    private final ActivityService  activityService;

    @Override
    @PostMapping("/sign-up")
    public ResponseEntity<SignUpResponse> createUser(@Valid  @RequestBody CreateUserRequest request) {
        Auth user = sessionService.registerUser(userMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(user));
    }

    @Override
    @PostMapping("/sign-in")
    public ResponseEntity<SignUpResponse> authenticateUser(@Valid @RequestBody SignInRequest request) {
        Auth user = sessionService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserResponse> aboutUser() {
        User user = userSessionService.getCurrentUser();
        return ResponseEntity.ok(userMapper.toUserResponseDTO(user));
    }

    @Override
    @GetMapping
    public ResponseEntity<UsersResponse> findAll() {
        Set<User> result = userService.findAll();
        return ResponseEntity.ok(userMapper.toUsersResponse(result));
    }

    @Override
    @GetMapping("/me/statistics")
    public ResponseEntity<StatisticsResponse>  getStatistics() {
        Statistics statistics = userService.loadStatisticsByUser(userSessionService.getCurrentUser().getId());
        return ResponseEntity.ok(userMapper.toStatisticsDTO(statistics));
    }

    @Override
    @GetMapping("/me/activities")
    public ResponseEntity<ActivityListResponse> getUsersActivity() {
        Set<ActivityParticipant> participants = activityService.findCurrentUserActivities();
        ActivityListResponse response =  userMapper.toActivityListResponse(participants);
        return ResponseEntity.ok(response);
    }
}
