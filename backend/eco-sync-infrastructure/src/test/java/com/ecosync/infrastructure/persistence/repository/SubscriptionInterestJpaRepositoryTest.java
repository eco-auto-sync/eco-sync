package com.ecosync.infrastructure.persistence.repository;

import com.ecosync.domain.subscription.InterestType;
import com.ecosync.infrastructure.persistence.entity.SubscriptionEntity;
import com.ecosync.infrastructure.persistence.entity.SubscriptionInterestEntity;
import com.ecosync.infrastructure.support.TestDataJpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestDataJpaConfig.class)
class SubscriptionInterestJpaRepositoryTest {

    @Autowired
    private SubscriptionInterestJpaRepository sut;

    @Autowired
    private SubscriptionJpaRepository subscriptionRepository;

    private SubscriptionEntity savedSubscription;

    @BeforeEach
    void setUp() {
        savedSubscription = subscriptionRepository.save(SubscriptionEntity.builder()
                .email("test@example.com")
                .calendarToken("token-uuid")
                .build());

        sut.save(SubscriptionInterestEntity.builder()
                .subscription(savedSubscription)
                .interestType(InterestType.COUNTRY)
                .interestValue("KR")
                .build());

        sut.save(SubscriptionInterestEntity.builder()
                .subscription(savedSubscription)
                .interestType(InterestType.COUNTRY)
                .interestValue("US")
                .build());
    }

    @Nested
    @DisplayName("softDeleteBySubscriptionId()")
    class SoftDeleteBySubscriptionId {

        @Test
        @DisplayName("구독 ID에 해당하는 활성 관심사를 모두 소프트딜리트한다")
        void softDeleteBySubscriptionId_softDeletesAllActiveInterests() {
            // when
            sut.softDeleteBySubscriptionId(savedSubscription.getId(), LocalDateTime.now());

            // then
            List<SubscriptionInterestEntity> remaining =
                    sut.findBySubscription_IdAndDeletedAtIsNull(savedSubscription.getId());
            assertThat(remaining).isEmpty();
        }

        @Test
        @DisplayName("다른 구독의 관심사는 영향을 받지 않는다")
        void softDeleteBySubscriptionId_doesNotAffectOtherSubscriptions() {
            // given
            SubscriptionEntity otherSubscription = subscriptionRepository.save(SubscriptionEntity.builder()
                    .email("other@example.com")
                    .calendarToken("other-token")
                    .build());
            sut.save(SubscriptionInterestEntity.builder()
                    .subscription(otherSubscription)
                    .interestType(InterestType.COUNTRY)
                    .interestValue("JP")
                    .build());

            // when
            sut.softDeleteBySubscriptionId(savedSubscription.getId(), LocalDateTime.now());

            // then
            List<SubscriptionInterestEntity> otherRemaining =
                    sut.findBySubscription_IdAndDeletedAtIsNull(otherSubscription.getId());
            assertThat(otherRemaining).hasSize(1);
        }
    }
}
