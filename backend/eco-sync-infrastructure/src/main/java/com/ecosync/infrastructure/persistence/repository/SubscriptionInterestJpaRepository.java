package com.ecosync.infrastructure.persistence.repository;

import com.ecosync.infrastructure.persistence.entity.SubscriptionInterestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionInterestJpaRepository extends JpaRepository<SubscriptionInterestEntity, Long> {

    List<SubscriptionInterestEntity> findBySubscription_IdAndDeletedAtIsNull(Long subscriptionId);

    @Modifying
    @Query("UPDATE SubscriptionInterestEntity si SET si.deletedAt = :now WHERE si.subscription.id = :subscriptionId AND si.deletedAt IS NULL")
    void softDeleteBySubscriptionId(@Param("subscriptionId") Long subscriptionId, @Param("now") LocalDateTime now);
}
