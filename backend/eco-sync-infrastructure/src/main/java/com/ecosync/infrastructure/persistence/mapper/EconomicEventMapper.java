package com.ecosync.infrastructure.persistence.mapper;

import com.ecosync.domain.event.EconomicEvent;
import com.ecosync.infrastructure.persistence.entity.EconomicEventEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EconomicEventMapper {

    EconomicEvent toDomain(EconomicEventEntity entity);

    EconomicEventEntity toEntity(EconomicEvent domain);
}
