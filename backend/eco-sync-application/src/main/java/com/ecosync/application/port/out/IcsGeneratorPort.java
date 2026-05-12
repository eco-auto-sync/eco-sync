package com.ecosync.application.port.out;

import com.ecosync.domain.event.EconomicEvent;

import java.util.List;

public interface IcsGeneratorPort {
    byte[] generate(List<EconomicEvent> events);
}
