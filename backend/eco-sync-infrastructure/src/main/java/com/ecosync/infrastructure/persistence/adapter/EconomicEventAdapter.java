package com.ecosync.infrastructure.persistence.adapter;

import com.ecosync.application.port.out.EconomicEventPort;
import com.ecosync.domain.event.EconomicEvent;
import com.ecosync.domain.event.EventCategory;
import com.ecosync.infrastructure.persistence.entity.EconomicEventEntity;
import com.ecosync.infrastructure.persistence.mapper.EconomicEventMapper;
import com.ecosync.infrastructure.persistence.repository.EconomicEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EconomicEventAdapter implements EconomicEventPort {

    private final EconomicEventJpaRepository repository;
    private final EconomicEventMapper mapper;

    @Override
    public List<EconomicEvent> findHolidaysByExchanges(List<String> exchanges) {
        return repository.findByCategoryAndExchangeIn(EventCategory.HOLIDAY, exchanges)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void upsertAll(List<EconomicEvent> events) {
        for (EconomicEvent event : events) {
            EconomicEventEntity entity = repository.findByUid(event.getUid())
                    .map(existing -> buildUpdated(existing, event))
                    .orElseGet(() -> mapper.toEntity(event));
            repository.save(entity);
        }
    }

    private EconomicEventEntity buildUpdated(EconomicEventEntity existing, EconomicEvent event) {
        return EconomicEventEntity.builder()
                .id(existing.getId())
                .uid(event.getUid())
                .title(event.getTitle())
                .eventDate(event.getEventDate())
                .eventTime(event.getEventTime())
                .countryCode(event.getCountryCode())
                .exchange(event.getExchange())
                .ticker(event.getTicker())
                .category(event.getCategory())
                .importance(event.getImportance())
                .isClosed(event.getIsClosed())
                .build();
    }
}
