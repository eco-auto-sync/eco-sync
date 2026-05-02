package com.ecosync.domain.event;

import com.ecosync.domain.common.BaseUpdated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@SuperBuilder
@NoArgsConstructor
public class EconomicEvent extends BaseUpdated {
    private Long id;
    private String uid;
    private String title;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String countryCode;
    private String exchange;
    private String ticker;
    private EventCategory category;
    private Importance importance;
    private Boolean isClosed;
}
