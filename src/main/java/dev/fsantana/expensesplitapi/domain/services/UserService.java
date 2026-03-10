package dev.fsantana.expensesplitapi.domain.services;

import dev.fsantana.expensesplitapi.domain.models.Statistics;
import dev.fsantana.expensesplitapi.domain.models.User;

import java.util.Set;
import java.util.UUID;

public interface UserService {

     Set<User> findAll();

     Statistics loadStatisticsByUser(UUID id);
}
