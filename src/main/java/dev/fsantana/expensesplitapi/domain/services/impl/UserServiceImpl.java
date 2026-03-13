package dev.fsantana.expensesplitapi.domain.services.impl;

import dev.fsantana.expensesplitapi.domain.models.Statistics;
import dev.fsantana.expensesplitapi.domain.models.User;
import dev.fsantana.expensesplitapi.domain.repositories.StatisticsRepository;
import dev.fsantana.expensesplitapi.domain.repositories.UserRepository;
import dev.fsantana.expensesplitapi.domain.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StatisticsRepository statisticsRepository;

    @Override
    public Set<User> findAll() {
        return new HashSet<>(userRepository.findAll());
    }

    @Override
    public Statistics loadStatisticsByUser(UUID id) {
        return statisticsRepository.loadStatisticsById(id);
    }
}
