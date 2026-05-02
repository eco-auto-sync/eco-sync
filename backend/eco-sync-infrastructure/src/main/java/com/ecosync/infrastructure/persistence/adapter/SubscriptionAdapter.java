package com.ecosync.infrastructure.persistence.adapter;

import com.ecosync.application.port.out.SubscriptionPort;
import com.ecosync.domain.subscription.Subscription;
import com.ecosync.domain.subscription.SubscriptionInterest;
import com.ecosync.infrastructure.persistence.entity.SubscriptionEntity;
import com.ecosync.infrastructure.persistence.mapper.SubscriptionMapper;
import com.ecosync.infrastructure.persistence.repository.SubscriptionInterestJpaRepository;
import com.ecosync.infrastructure.persistence.repository.SubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SubscriptionAdapter implements SubscriptionPort {

    private final SubscriptionJpaRepository subscriptionRepository;
    private final SubscriptionInterestJpaRepository interestRepository;
    private final SubscriptionMapper mapper;

    @Override
    public Optional<Subscription> findByEmail(String email) {
        return subscriptionRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<Subscription> findByCalendarToken(String calendarToken) {
        return subscriptionRepository.findByCalendarToken(calendarToken).map(mapper::toDomain);
    }

    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionEntity entity;
        if (subscription.getId() == null) {
            entity = mapper.toEntity(subscription);
        } else {
            entity = subscriptionRepository.findById(subscription.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscription.getId()));
            if (subscription.isActive()) {
                entity.restore();
            } else {
                entity.softDelete();
            }
        }
        return mapper.toDomain(subscriptionRepository.save(entity));
    }

    @Override
    public void saveInterests(List<SubscriptionInterest> interests) {
        interests.forEach(interest -> interestRepository.save(mapper.interestToEntity(interest)));
    }

    @Override
    public void softDeleteInterestsBySubscriptionId(Long subscriptionId) {
        interestRepository.softDeleteBySubscriptionId(subscriptionId, LocalDateTime.now());
    }
}
