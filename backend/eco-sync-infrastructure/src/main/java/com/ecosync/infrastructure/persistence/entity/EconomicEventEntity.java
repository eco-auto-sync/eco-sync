package com.ecosync.infrastructure.persistence.entity;

import com.ecosync.domain.event.EventCategory;
import com.ecosync.domain.event.Importance;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "economic_events")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EconomicEventEntity extends BaseUpdatedEntity {

    @Comment("경제 이벤트 PK")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("iCal UID — 배치 upsert 기준 키")
    @Column(nullable = false, unique = true)
    private String uid;

    @Comment("이벤트 제목")
    @Column(nullable = false)
    private String title;

    @Comment("이벤트 날짜 (UTC)")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Comment("이벤트 시각 (NULL = 종일 이벤트)")
    @Column(name = "event_time")
    private LocalTime eventTime;

    @Comment("국가 코드 (KR, US 등)")
    @Column(name = "country_code", nullable = false, length = 10)
    private String countryCode;

    @Comment("거래소 코드 (KRX, NYSE 등) — HOLIDAY 전용")
    @Column(length = 20)
    private String exchange;

    @Comment("종목 코드 (AAPL, 005930 등) — EARNINGS 전용")
    @Column(length = 20)
    private String ticker;

    @Comment("이벤트 유형 (HOLIDAY | INDICATOR | EARNINGS)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventCategory category;

    @Comment("중요도 (HIGH | MEDIUM | LOW) — INDICATOR 전용")
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Importance importance;

    @Comment("완전 휴장 여부 — HOLIDAY 전용")
    @Column(name = "is_closed")
    private Boolean isClosed;
}
