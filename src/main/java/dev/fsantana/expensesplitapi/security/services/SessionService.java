package dev.fsantana.expensesplitapi.security.services;

import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.security.models.Auth;

public interface SessionService {

    Auth registerUser(User user);
    Auth login(String email, String password);

}
