package com.ecosync.application.service;

import com.ecosync.domain.subscription.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CountryServiceTest {

    private CountryService countryService;

    @BeforeEach
    void setUp() {
        countryService = new CountryService();
    }

    @Nested
    @DisplayName("getCountries() — 지원 국가 목록 조회")
    class GetCountries {

        @Test
        @DisplayName("Phase 1 지원 국가 7개를 반환한다")
        void getCountries_returnsAllSupportedCountries() {
            // when
            List<Country> countries = countryService.getCountries();

            // then
            assertThat(countries).hasSize(7);
        }

        @Test
        @DisplayName("지원 국가 코드를 모두 포함한다")
        void getCountries_containsExpectedCodes() {
            // when
            List<String> codes = countryService.getCountries().stream()
                    .map(Country::code)
                    .toList();

            // then
            assertThat(codes).containsExactly("KR", "US", "JP", "CN", "HK", "GB", "DE");
        }

        @Test
        @DisplayName("각 국가는 코드, 이름, 거래소, 국기를 모두 갖는다")
        void getCountries_eachCountryHasAllFields() {
            // when
            List<Country> countries = countryService.getCountries();

            // then
            assertThat(countries).allSatisfy(country -> {
                assertThat(country.code()).isNotBlank();
                assertThat(country.name()).isNotBlank();
                assertThat(country.exchange()).isNotBlank();
                assertThat(country.flag()).isNotBlank();
            });
        }
    }
}
