package com.ecosync.application.port.out;

import com.ecosync.domain.event.EconomicEvent;

import java.util.List;

public interface EconomicEventPort {
    List<EconomicEvent> findHolidaysByExchanges(List<String> exchanges);
    void upsertAll(List<EconomicEvent> events);
}
