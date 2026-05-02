package com.ecosync.infrastructure.persistence.entity;

import com.ecosync.domain.subscription.InterestType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "subscription_interests")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionInterestEntity extends BaseSoftDeleteEntity {

    @Comment("구독 관심사 PK")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("구독 FK")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionEntity subscription;

    @Comment("관심사 유형 (COUNTRY | IMPORTANCE | TICKER)")
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false, length = 20)
    private InterestType interestType;

    @Comment("관심사 값 (KR, US | HIGH, MEDIUM | AAPL)")
    @Column(name = "interest_value", nullable = false, length = 50)
    private String interestValue;
}
