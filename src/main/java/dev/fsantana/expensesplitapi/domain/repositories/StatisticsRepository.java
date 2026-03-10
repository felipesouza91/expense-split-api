package dev.fsantana.expensesplitapi.domain.repositories;

import dev.fsantana.expensesplitapi.domain.models.Statistics;

import java.util.UUID;

public interface StatisticsRepository {

    Statistics loadStatisticsById(UUID id);
}
