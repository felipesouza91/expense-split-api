package dev.fsantana.expensesplitapi.configs;

import dev.fsantana.expensesplitapi.domain.models.Activity;
import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import dev.fsantana.expensesplitapi.domain.models.Expense;
import dev.fsantana.expensesplitapi.domain.models.ExpenseParticipant;
import dev.fsantana.expensesplitapi.domain.models.ExpensePayment;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.repositories.ActivityParticipantRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ActivityRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpenseParticipantRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpensePaymentRepository;
import dev.fsantana.expensesplitapi.domain.repositories.ExpenseRepository;
import dev.fsantana.expensesplitapi.domain.repositories.UserRepository;
import dev.fsantana.expensesplitapi.security.models.Auth;
import dev.fsantana.expensesplitapi.security.services.SessionService;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Testcontainers
@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT )
@TestPropertySource("classpath:application-test.yaml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class TestIntegrationConfig {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
  }

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserRepository userDataProvider;

    @Autowired
    protected ActivityRepository activityDataProvider;

    @Autowired
    protected ActivityParticipantRepository activityParticipantRepository;

    @Autowired
    protected ExpenseParticipantRepository expenseParticipantRepository;

    @Autowired
    protected ExpensePaymentRepository expensePaymentRepository;

    @Autowired
    protected ExpenseRepository expenseRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine").withReuse(true);


    protected User createUser() {
        String blankPassword=  "12345678";
        User user = new User();
        user.setName("Jonh Doe 1 ");
        user.setEmail("jonhdoe1@email.com");
        user.setPasswordHash(blankPassword);

        Optional<User> byEmail = userDataProvider.findByEmail(user.getEmail());
        if(byEmail.isEmpty()) {
            sessionService.registerUser(user);
        } else {
            byEmail.get().setPasswordHash(blankPassword);
            return byEmail.get();
        }
        user.setPasswordHash(blankPassword);
        return user;
    }

    protected User createUserB() {
        String blankPassword = "12345678";
        User user = new User();
        user.setName("Jonh Doe 2 ");
        user.setEmail("jonhdoe2@email.com");
        user.setPasswordHash(blankPassword);

        user.setPasswordHash(passwordEncoder.encode(blankPassword));
        Optional<User> savedUser = userDataProvider.
                findByEmail(user.getEmail());
        if(savedUser.isPresent()) {
            return savedUser.get();
        }
        userDataProvider.saveAndFlush(user);
        return user;
    }

    protected User createUserC() {
        String blankPassword = "12345678";
        User user = new User();
        user.setName("Jonh Doe 23 ");
        user.setEmail("jonhdoe23@email.com");
        user.setPasswordHash(blankPassword);

        user.setPasswordHash(passwordEncoder.encode(blankPassword));
        Optional<User> savedUser = userDataProvider.
                findByEmail(user.getEmail());
        if(savedUser.isPresent()) {
            return savedUser.get();
        }
        userDataProvider.saveAndFlush(user);
        return user;
    }

    protected Auth token() {
        User user = createUser();
        return sessionService.login(user.getEmail(), user.getPasswordHash());
    }

    protected Activity createActivity(User user){
        ActivityParticipant activityParticipant = new ActivityParticipant();
        activityParticipant.setUser(user);
        Activity activity = new Activity();
        activity.setName("Test Activity");

        activity.setActivityDate(OffsetDateTime.now());
        activity.setParticipants(List.of(activityParticipant));
        activityParticipant.setActivity(activity);
        return activityDataProvider.save(activity);
    }

    protected Activity createActivityWithoutParticipants(){

        Activity activity = new Activity();
        activity.setName("Test Activity");

        activity.setActivityDate(OffsetDateTime.now());

        return activityDataProvider.save(activity);
    }

    protected Activity createActivityWithParticipants(User authUser, Set<User> participants) {
        Activity activity = createActivity(authUser);
        List<ActivityParticipant> list = new ArrayList<>();

        participants.forEach(item -> {
            ActivityParticipant participant = new ActivityParticipant();
            participant.setUser(item);
            participant.setActivity(activity);
            list.add(participant);
        });
        list.addAll(activity.getParticipants());
        activity.setParticipants(list);
        activityDataProvider.save(activity);
        return activity;
    }

    @Transactional
    protected Expense createExpenseForActivity(UUID payerId, Activity activity, Long amountInCents, List<UUID> participants) {
        Expense expense = new Expense();
        expense.setName(Instancio.create(String.class));
        if(Objects.nonNull(payerId)) {
            userDataProvider.findById(payerId).ifPresent(expense::setPayer);
        }
        Set<ExpenseParticipant> expenseParticipants = participants.stream().map(item -> {
            ExpenseParticipant expenseParticipant = new ExpenseParticipant();
            User user = userDataProvider.findById(item).get();
            expenseParticipant.setUser(user);
            return expenseParticipant;
        }).collect(Collectors.toSet());
        List<ActivityParticipant> byActivityId = activityParticipantRepository.findByActivityId(activity.getId());
        activity.setParticipants(byActivityId);
        Long amountPerParticipant = 0L;
        if(!participants.isEmpty()) {
            amountPerParticipant = amountInCents / participants.size();
        }
        Set<ExpenseParticipant> expenseParticipantsSet = new HashSet<>();
        for (ExpenseParticipant expenseParticipant : expenseParticipants) {
            Optional<ActivityParticipant> isParticipantInActivity = activity.getParticipants().stream()
                    .filter(item -> item.getUser().getId()
                            .equals(expenseParticipant.getUser().getId())).findAny();

            if (isParticipantInActivity.isEmpty()) {
                ActivityParticipant activityParticipant = new ActivityParticipant();
                activityParticipant.setUser(expenseParticipant.getUser());
                activityParticipant.setActivity(activity);
                activity.getParticipants().add(activityParticipant);
            }
            expenseParticipant.setAmountOwedInCents(amountPerParticipant);
            expenseParticipant.setUser(expenseParticipant.getUser());
            expenseParticipant.setExpense(expense);

            expenseParticipantsSet.add(expenseParticipant);
        }
        expense.setAmountInCents(amountInCents);
        expense.setExpenseParticipants(expenseParticipantsSet);
        expense.setActivity(activity);
        activityDataProvider.save(activity);
        expenseRepository.save(expense);
        expenseParticipantRepository.saveAll(expenseParticipantsSet);
        return expense;
    }

    public ExpensePayment createExpensePayment(Expense expense, Long amountInCents, User debtor) {
        ExpensePayment expensePayment = new ExpensePayment();
        expensePayment.setExpense(expense);
        expensePayment.setDebtor(debtor);
        expensePayment.setAmountPaidInCents(amountInCents);
        return expensePaymentRepository.save(expensePayment);
    }

}