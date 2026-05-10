package com.ecosync.api.controller;

import com.ecosync.api.config.SecurityConfig;
import com.ecosync.api.support.JsonDataEncoder;
import com.ecosync.application.port.in.GetCountriesUseCase;
import com.ecosync.domain.subscription.Country;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CountryController.class)
@Import({SecurityConfig.class, JsonDataEncoder.class})
class CountryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCountriesUseCase getCountriesUseCase;

    @Nested
    @DisplayName("GET /api/countries — 지원 국가 목록 조회")
    class GetCountries {

        private static final List<Country> SUPPORTED_COUNTRIES = List.of(
                new Country("KR", "한국", "KRX", "🇰🇷"),
                new Country("US", "미국", "NYSE, NASDAQ", "🇺🇸")
        );

        @Test
        @DisplayName("200과 국가 목록을 반환한다")
        void getCountries_success() throws Exception {
            // given
            given(getCountriesUseCase.getCountries()).willReturn(SUPPORTED_COUNTRIES);

            // when & then
            mockMvc.perform(get("/api/countries"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].code").value("KR"))
                    .andExpect(jsonPath("$[0].name").value("한국"))
                    .andExpect(jsonPath("$[0].exchange").value("KRX"))
                    .andExpect(jsonPath("$[0].flag").value("🇰🇷"));

            then(getCountriesUseCase).should().getCountries();
        }

        @Test
        @DisplayName("국가가 없으면 빈 목록을 반환한다")
        void getCountries_empty_returnsEmptyList() throws Exception {
            // given
            given(getCountriesUseCase.getCountries()).willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/countries"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));

            then(getCountriesUseCase).should().getCountries();
        }
    }
}
