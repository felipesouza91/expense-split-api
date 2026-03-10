package dev.fsantana.expensesplitapi.domain.repositories;

import dev.fsantana.expensesplitapi.domain.models.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    Optional<Activity> findByIdAndParticipantsUserId(UUID id, UUID participantsUserId);

    List<Activity>  findByParticipantsUserId(UUID userId);
}
