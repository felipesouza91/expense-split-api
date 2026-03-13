package dev.fsantana.expensesplitapi.security.services.impl;

import dev.fsantana.expensesplitapi.domain.exceptions.AppEntityNotFound;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.repositories.UserRepository;
import dev.fsantana.expensesplitapi.security.models.Auth;
import dev.fsantana.expensesplitapi.security.services.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {

    private final UserRepository userDataProvider;
    private User user;


    @Override
    public User getCurrentUser() {
        Auth authentication = (Auth)  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.isNull(user) || authentication.getUser().getId() != user.getId()) {
           user = userDataProvider.findById(authentication.getUser().getId()).orElseThrow(() -> new AppEntityNotFound("User not found"));
        }
        return user;
    }

}