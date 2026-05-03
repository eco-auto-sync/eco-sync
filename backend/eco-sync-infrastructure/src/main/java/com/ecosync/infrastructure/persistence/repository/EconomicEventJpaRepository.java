package com.ecosync.infrastructure.persistence.repository;

import com.ecosync.domain.event.EventCategory;
import com.ecosync.infrastructure.persistence.entity.EconomicEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EconomicEventJpaRepository extends JpaRepository<EconomicEventEntity, Long> {
    Optional<EconomicEventEntity> findByUid(String uid);
    List<EconomicEventEntity> findByCategoryAndExchangeIn(EventCategory category, List<String> exchanges);
}
