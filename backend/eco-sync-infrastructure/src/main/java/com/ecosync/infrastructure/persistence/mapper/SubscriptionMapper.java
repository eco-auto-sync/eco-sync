package com.ecosync.infrastructure.persistence.mapper;

import com.ecosync.domain.subscription.Subscription;
import com.ecosync.domain.subscription.SubscriptionInterest;
import com.ecosync.infrastructure.persistence.entity.SubscriptionInterestEntity;
import com.ecosync.infrastructure.persistence.entity.SubscriptionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    Subscription toDomain(SubscriptionEntity entity);

    SubscriptionEntity toEntity(Subscription domain);

    @Mapping(source = "subscription.id", target = "subscriptionId")
    SubscriptionInterest interestToDomain(SubscriptionInterestEntity entity);

    @Mapping(source = "subscriptionId", target = "subscription.id")
    SubscriptionInterestEntity interestToEntity(SubscriptionInterest domain);
}
