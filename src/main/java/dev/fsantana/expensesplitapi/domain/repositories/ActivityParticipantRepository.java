package dev.fsantana.expensesplitapi.domain.repositories;

import dev.fsantana.expensesplitapi.domain.models.ActivityParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, UUID> {

    Set<ActivityParticipant> findByUserId(UUID userId);

    Optional<ActivityParticipant> findByUserIdAndActivityId(UUID id, UUID activityId);

    List<ActivityParticipant> findByActivityId(UUID activityId);
}
