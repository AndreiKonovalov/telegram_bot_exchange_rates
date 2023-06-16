package ru.konovalov.bot_exchange_rates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.konovalov.bot_exchange_rates.model.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
