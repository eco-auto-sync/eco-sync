package com.ecosync.infrastructure.persistence.repository;

import com.ecosync.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, Long> {
    Optional<SubscriptionEntity> findByEmail(String email);
    Optional<SubscriptionEntity> findByCalendarToken(String calendarToken);
}
