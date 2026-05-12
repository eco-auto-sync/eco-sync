package com.ecosync.application.service;

import com.ecosync.application.exception.EcoSyncException;
import com.ecosync.application.exception.ErrorCode;
import com.ecosync.application.port.in.GetCalendarUseCase;
import com.ecosync.application.port.in.GetCountriesUseCase;
import com.ecosync.application.port.out.EconomicEventPort;
import com.ecosync.application.port.out.IcsGeneratorPort;
import com.ecosync.application.port.out.SubscriptionPort;
import com.ecosync.domain.subscription.Country;
import com.ecosync.domain.subscription.InterestType;
import com.ecosync.domain.subscription.Subscription;
import com.ecosync.domain.subscription.SubscriptionInterest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private SubscriptionPort subscriptionPort;

    @Mock
    private EconomicEventPort economicEventPort;

    @Mock
    private GetCountriesUseCase getCountriesUseCase;

    @Mock
    private IcsGeneratorPort icsGeneratorPort;

    @InjectMocks
    private CalendarService sut;

    private static final String TOKEN = "test-token-uuid";
    private static final List<Country> SUPPORTED_COUNTRIES = List.of(
            new Country("KR", "한국", "KRX", "🇰🇷"),
            new Country("US", "미국", "NYSE, NASDAQ", "🇺🇸")
    );

    @Nested
    @DisplayName("getCalendar() — ICS 캘린더 조회")
    class GetCalendar {

        @Test
        @DisplayName("활성 구독이면 ICS 바이트를 반환한다")
        void getCalendar_activeSubscription_returnsIcsContent() {
            // given
            Subscription subscription = Subscription.builder()
                    .id(1L).email("test@example.com").calendarToken(TOKEN).build();

            List<SubscriptionInterest> interests = List.of(
                    SubscriptionInterest.builder()
                            .subscriptionId(1L).interestType(InterestType.COUNTRY).interestValue("KR").build()
            );
            byte[] icsBytes = "BEGIN:VCALENDAR".getBytes();

            given(subscriptionPort.findByCalendarToken(TOKEN)).willReturn(Optional.of(subscription));
            given(subscriptionPort.findActiveInterestsBySubscriptionId(1L)).willReturn(interests);
            given(getCountriesUseCase.getCountries()).willReturn(SUPPORTED_COUNTRIES);
            given(economicEventPort.findHolidaysByExchanges(any())).willReturn(List.of());
            given(icsGeneratorPort.generate(any())).willReturn(icsBytes);

            // when
            GetCalendarUseCase.Result result = sut.getCalendar(TOKEN);

            // then
            assertThat(result.icsContent()).isEqualTo(icsBytes);
            then(economicEventPort).should().findHolidaysByExchanges(List.of("KRX"));
        }

        @Test
        @DisplayName("쉼표로 구분된 거래소는 분리해서 조회한다")
        void getCalendar_multipleExchanges_splitAndQuery() {
            // given
            Subscription subscription = Subscription.builder()
                    .id(1L).email("test@example.com").calendarToken(TOKEN).build();

            List<SubscriptionInterest> interests = List.of(
                    SubscriptionInterest.builder()
                            .subscriptionId(1L).interestType(InterestType.COUNTRY).interestValue("US").build()
            );

            given(subscriptionPort.findByCalendarToken(TOKEN)).willReturn(Optional.of(subscription));
            given(subscriptionPort.findActiveInterestsBySubscriptionId(1L)).willReturn(interests);
            given(getCountriesUseCase.getCountries()).willReturn(SUPPORTED_COUNTRIES);
            given(economicEventPort.findHolidaysByExchanges(any())).willReturn(List.of());
            given(icsGeneratorPort.generate(any())).willReturn(new byte[0]);

            // when
            sut.getCalendar(TOKEN);

            // then
            then(economicEventPort).should().findHolidaysByExchanges(
                    argThat(exchanges -> exchanges.containsAll(List.of("NYSE", "NASDAQ")) && exchanges.size() == 2)
            );
        }

        @Test
        @DisplayName("토큰에 해당하는 구독이 없으면 CALENDAR_NOT_FOUND 예외를 던진다")
        void getCalendar_tokenNotFound_throwsCalendarNotFound() {
            // given
            given(subscriptionPort.findByCalendarToken(TOKEN)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.getCalendar(TOKEN))
                    .isInstanceOf(EcoSyncException.class)
                    .extracting(e -> ((EcoSyncException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CALENDAR_NOT_FOUND);

            then(subscriptionPort).should(never()).findActiveInterestsBySubscriptionId(any());
        }

        @Test
        @DisplayName("소프트딜리트된 구독이면 CALENDAR_NOT_FOUND 예외를 던진다")
        void getCalendar_softDeletedSubscription_throwsCalendarNotFound() {
            // given
            Subscription deletedSubscription = Subscription.builder()
                    .id(1L).email("test@example.com").calendarToken(TOKEN).build();
            deletedSubscription.softDelete();

            given(subscriptionPort.findByCalendarToken(TOKEN)).willReturn(Optional.of(deletedSubscription));

            // when & then
            assertThatThrownBy(() -> sut.getCalendar(TOKEN))
                    .isInstanceOf(EcoSyncException.class)
                    .extracting(e -> ((EcoSyncException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CALENDAR_NOT_FOUND);

            then(subscriptionPort).should(never()).findActiveInterestsBySubscriptionId(any());
        }
    }
}
