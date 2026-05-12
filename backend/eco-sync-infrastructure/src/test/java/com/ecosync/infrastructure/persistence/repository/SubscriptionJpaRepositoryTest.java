package com.ecosync.infrastructure.persistence.repository;

import com.ecosync.infrastructure.persistence.entity.SubscriptionEntity;
import com.ecosync.infrastructure.support.TestDataJpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestDataJpaConfig.class)
class SubscriptionJpaRepositoryTest {

    @Autowired
    private SubscriptionJpaRepository sut;

    private SubscriptionEntity savedSubscription;

    @BeforeEach
    void setUp() {
        savedSubscription = sut.save(SubscriptionEntity.builder()
                .email("test@example.com")
                .calendarToken("token-uuid")
                .build());
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("존재하는 이메일이면 구독을 반환한다")
        void findByEmail_existingEmail_returnsSubscription() {
            // when
            Optional<SubscriptionEntity> result = sut.findByEmail("test@example.com");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
            assertThat(result.get().getCalendarToken()).isEqualTo("token-uuid");
        }

        @Test
        @DisplayName("존재하지 않는 이메일이면 빈 값을 반환한다")
        void findByEmail_nonExistingEmail_returnsEmpty() {
            // when
            Optional<SubscriptionEntity> result = sut.findByEmail("none@example.com");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCalendarToken()")
    class FindByCalendarToken {

        @Test
        @DisplayName("존재하는 토큰이면 구독을 반환한다")
        void findByCalendarToken_existingToken_returnsSubscription() {
            // when
            Optional<SubscriptionEntity> result = sut.findByCalendarToken("token-uuid");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getCalendarToken()).isEqualTo("token-uuid");
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 토큰이면 빈 값을 반환한다")
        void findByCalendarToken_nonExistingToken_returnsEmpty() {
            // when
            Optional<SubscriptionEntity> result = sut.findByCalendarToken("invalid-token");

            // then
            assertThat(result).isEmpty();
        }
    }
}
