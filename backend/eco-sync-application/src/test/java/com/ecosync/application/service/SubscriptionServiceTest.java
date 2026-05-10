package com.ecosync.application.service;

import com.ecosync.application.exception.EcoSyncException;
import com.ecosync.application.exception.ErrorCode;
import com.ecosync.application.port.in.CreateSubscriptionUseCase.Command;
import com.ecosync.application.port.in.CreateSubscriptionUseCase.Result;
import com.ecosync.application.port.in.GetCountriesUseCase;
import com.ecosync.application.port.out.SubscriptionPort;
import com.ecosync.domain.subscription.Country;
import com.ecosync.domain.subscription.Subscription;
import com.ecosync.domain.subscription.SubscriptionInterest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionPort subscriptionPort;

    @Mock
    private GetCountriesUseCase getCountriesUseCase;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Captor
    private ArgumentCaptor<List<SubscriptionInterest>> interestsCaptor;

    private static final String EMAIL = "test@example.com";
    private static final List<String> COUNTRY_CODES = List.of("KR", "US");
    private static final List<Country> SUPPORTED_COUNTRIES = List.of(
            new Country("KR", "한국", "KRX", "🇰🇷"),
            new Country("US", "미국", "NYSE, NASDAQ", "🇺🇸")
    );

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(subscriptionService, "baseUrl", "http://localhost:8080");
    }

    @Nested
    @DisplayName("create() — 구독 생성")
    class Create {

        @Test
        @DisplayName("신규 이메일이면 구독을 생성하고 관심사를 저장한다")
        void create_newEmail_savesSubscriptionAndInterests() {
            // given
            Command command = new Command(EMAIL, COUNTRY_CODES);
            Subscription savedSubscription = Subscription.builder()
                    .id(1L)
                    .email(EMAIL)
                    .calendarToken("token-uuid")
                    .build();

            given(getCountriesUseCase.getCountries()).willReturn(SUPPORTED_COUNTRIES);
            given(subscriptionPort.findByEmail(EMAIL)).willReturn(Optional.empty());
            given(subscriptionPort.save(any())).willReturn(savedSubscription);

            // when
            Result result = subscriptionService.create(command);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.calendarUrl()).isEqualTo("http://localhost:8080/api/calendar/token-uuid/subscribe");

            then(subscriptionPort).should().save(any(Subscription.class));
            then(subscriptionPort).should().saveInterests(any());
        }

        @Test
        @DisplayName("관심사는 요청한 국가 코드 수만큼 저장된다")
        void create_savesInterestsMatchingCountryCodes() {
            // given
            Command command = new Command(EMAIL, COUNTRY_CODES);
            Subscription savedSubscription = Subscription.builder()
                    .id(1L).email(EMAIL).calendarToken("token-uuid").build();

            given(getCountriesUseCase.getCountries()).willReturn(SUPPORTED_COUNTRIES);
            given(subscriptionPort.findByEmail(EMAIL)).willReturn(Optional.empty());
            given(subscriptionPort.save(any())).willReturn(savedSubscription);

            // when
            subscriptionService.create(command);

            // then
            then(subscriptionPort).should().saveInterests(interestsCaptor.capture());
            assertThat(interestsCaptor.getValue()).hasSize(COUNTRY_CODES.size());
        }

        @Test
        @DisplayName("이미 구독 중인 이메일이면 DUPLICATE_SUBSCRIPTION 예외를 던진다")
        void create_activeEmail_throwsDuplicateSubscription() {
            // given
            Command command = new Command(EMAIL, COUNTRY_CODES);
            Subscription activeSubscription = Subscription.builder()
                    .id(1L).email(EMAIL).calendarToken("token-uuid").build();

            given(getCountriesUseCase.getCountries()).willReturn(SUPPORTED_COUNTRIES);
            given(subscriptionPort.findByEmail(EMAIL)).willReturn(Optional.of(activeSubscription));

            // when & then
            assertThatThrownBy(() -> subscriptionService.create(command))
                    .isInstanceOf(EcoSyncException.class)
                    .extracting(e -> ((EcoSyncException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_SUBSCRIPTION);

            then(subscriptionPort).should(never()).save(any());
        }

        @Test
        @DisplayName("소프트딜리트된 구독이면 복원 후 관심사를 교체한다")
        void create_softDeletedEmail_restoresSubscription() {
            // given
            Command command = new Command(EMAIL, COUNTRY_CODES);
            Subscription deletedSubscription = Subscription.builder()
                    .id(1L).email(EMAIL).calendarToken("token-uuid").build();
            deletedSubscription.softDelete();

            Subscription restoredSubscription = Subscription.builder()
                    .id(1L).email(EMAIL).calendarToken("token-uuid").build();

            given(getCountriesUseCase.getCountries()).willReturn(SUPPORTED_COUNTRIES);
            given(subscriptionPort.findByEmail(EMAIL)).willReturn(Optional.of(deletedSubscription));
            given(subscriptionPort.save(deletedSubscription)).willReturn(restoredSubscription);

            // when
            Result result = subscriptionService.create(command);

            // then
            assertThat(deletedSubscription.isActive()).isTrue();
            assertThat(result.id()).isEqualTo(1L);

            then(subscriptionPort).should().softDeleteInterestsBySubscriptionId(1L);
            then(subscriptionPort).should().saveInterests(any());
        }

        @Test
        @DisplayName("지원하지 않는 국가 코드가 포함되면 UNSUPPORTED_COUNTRY 예외를 던진다")
        void create_unsupportedCountryCode_throwsUnsupportedCountry() {
            // given
            Command command = new Command(EMAIL, List.of("KR", "XX"));

            given(getCountriesUseCase.getCountries()).willReturn(SUPPORTED_COUNTRIES);

            // when & then
            assertThatThrownBy(() -> subscriptionService.create(command))
                    .isInstanceOf(EcoSyncException.class)
                    .extracting(e -> ((EcoSyncException) e).getErrorCode())
                    .isEqualTo(ErrorCode.UNSUPPORTED_COUNTRY);

            then(subscriptionPort).shouldHaveNoInteractions();
        }
    }
}
