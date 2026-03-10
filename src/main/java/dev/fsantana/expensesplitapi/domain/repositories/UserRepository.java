package dev.fsantana.expensesplitapi.domain.repositories;

import dev.fsantana.expensesplitapi.domain.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}
