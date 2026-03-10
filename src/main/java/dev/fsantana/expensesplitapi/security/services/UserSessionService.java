package dev.fsantana.expensesplitapi.security.services;

import dev.fsantana.expensesplitapi.domain.models.User;

public interface UserSessionService {

    User getCurrentUser();
}