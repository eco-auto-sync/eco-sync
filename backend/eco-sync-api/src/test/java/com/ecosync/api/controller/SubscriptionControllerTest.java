package com.ecosync.api.controller;

import com.ecosync.api.config.SecurityConfig;
import com.ecosync.api.dto.request.CreateSubscriptionRequest;
import com.ecosync.api.support.JsonDataEncoder;
import com.ecosync.application.exception.EcoSyncException;
import com.ecosync.application.exception.ErrorCode;
import com.ecosync.api.dto.request.UpdateSubscriptionRequest;
import com.ecosync.application.port.in.CreateSubscriptionUseCase;
import com.ecosync.application.port.in.GetSubscriptionUseCase;
import com.ecosync.application.port.in.UpdateSubscriptionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
@Import({SecurityConfig.class, JsonDataEncoder.class})
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonDataEncoder jsonDataEncoder;

    @MockitoBean
    private CreateSubscriptionUseCase createSubscriptionUseCase;

    @MockitoBean
    private GetSubscriptionUseCase getSubscriptionUseCase;

    @MockitoBean
    private UpdateSubscriptionUseCase updateSubscriptionUseCase;

    @Nested
    @DisplayName("POST /api/subscriptions — 구독 생성")
    class CreateSubscription {

        private static final String EMAIL = "test@example.com";
        private static final List<String> COUNTRY_CODES = List.of("KR", "US");
        private static final String CALENDAR_URL = "http://localhost:8080/api/calendar/token-uuid/subscribe";

        @Test
        @DisplayName("유효한 요청이면 201과 구독 정보를 반환한다")
        void create_success() throws Exception {
            // given
            CreateSubscriptionRequest request = new CreateSubscriptionRequest(EMAIL, COUNTRY_CODES);
            given(createSubscriptionUseCase.create(any())).willReturn(
                    new CreateSubscriptionUseCase.Result(1L, CALENDAR_URL));

            // when & then
            mockMvc.perform(post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.calendarUrl").value(CALENDAR_URL));

            then(createSubscriptionUseCase).should().create(
                    new CreateSubscriptionUseCase.Command(EMAIL, COUNTRY_CODES));
        }

        @Test
        @DisplayName("이메일이 없으면 400을 반환한다")
        void create_missingEmail_returns400() throws Exception {
            // given
            CreateSubscriptionRequest request = new CreateSubscriptionRequest(null, COUNTRY_CODES);

            // when & then
            mockMvc.perform(post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_001"));

            then(createSubscriptionUseCase).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("이메일 형식이 올바르지 않으면 400을 반환한다")
        void create_invalidEmail_returns400() throws Exception {
            // given
            CreateSubscriptionRequest request = new CreateSubscriptionRequest("not-an-email", COUNTRY_CODES);

            // when & then
            mockMvc.perform(post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_001"));

            then(createSubscriptionUseCase).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("국가 코드 목록이 비어 있으면 400을 반환한다")
        void create_emptyCountryCodes_returns400() throws Exception {
            // given
            CreateSubscriptionRequest request = new CreateSubscriptionRequest(EMAIL, List.of());

            // when & then
            mockMvc.perform(post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_001"));

            then(createSubscriptionUseCase).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("이미 구독 중인 이메일이면 409를 반환한다")
        void create_duplicateEmail_returns409() throws Exception {
            // given
            CreateSubscriptionRequest request = new CreateSubscriptionRequest(EMAIL, COUNTRY_CODES);
            given(createSubscriptionUseCase.create(any()))
                    .willThrow(new EcoSyncException(ErrorCode.DUPLICATE_SUBSCRIPTION));

            // when & then
            mockMvc.perform(post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("SUBSCRIPTION_002"));

            then(createSubscriptionUseCase).should().create(any());
        }

        @Test
        @DisplayName("지원하지 않는 국가 코드가 포함되면 400을 반환한다")
        void create_unsupportedCountryCode_returns400() throws Exception {
            // given
            CreateSubscriptionRequest request = new CreateSubscriptionRequest(EMAIL, List.of("KR", "XX"));
            given(createSubscriptionUseCase.create(any()))
                    .willThrow(new EcoSyncException(ErrorCode.UNSUPPORTED_COUNTRY));

            // when & then
            mockMvc.perform(post("/api/subscriptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("SUBSCRIPTION_003"));

            then(createSubscriptionUseCase).should().create(any());
        }
    }

    @Nested
    @DisplayName("PUT /api/subscriptions/{id} — 구독 수정")
    class UpdateSubscription {

        private static final Long ID = 1L;
        private static final List<String> COUNTRY_CODES = List.of("KR", "US");
        private static final String CALENDAR_URL = "http://localhost:8080/api/calendar/token-uuid/subscribe";

        @Test
        @DisplayName("유효한 요청이면 200과 수정된 구독 정보를 반환한다")
        void update_success() throws Exception {
            // given
            UpdateSubscriptionRequest request = new UpdateSubscriptionRequest(COUNTRY_CODES);
            given(updateSubscriptionUseCase.update(any())).willReturn(
                    new UpdateSubscriptionUseCase.Result(ID, CALENDAR_URL));

            // when & then
            mockMvc.perform(put("/api/subscriptions/{id}", ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ID))
                    .andExpect(jsonPath("$.calendarUrl").value(CALENDAR_URL));

            then(updateSubscriptionUseCase).should().update(
                    new UpdateSubscriptionUseCase.Command(ID, COUNTRY_CODES));
        }

        @Test
        @DisplayName("국가 코드 목록이 비어 있으면 400을 반환한다")
        void update_emptyCountryCodes_returns400() throws Exception {
            // given
            UpdateSubscriptionRequest request = new UpdateSubscriptionRequest(List.of());

            // when & then
            mockMvc.perform(put("/api/subscriptions/{id}", ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_001"));

            then(updateSubscriptionUseCase).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("구독이 없으면 404를 반환한다")
        void update_notFound_returns404() throws Exception {
            // given
            UpdateSubscriptionRequest request = new UpdateSubscriptionRequest(COUNTRY_CODES);
            given(updateSubscriptionUseCase.update(any()))
                    .willThrow(new EcoSyncException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

            // when & then
            mockMvc.perform(put("/api/subscriptions/{id}", ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("SUBSCRIPTION_001"));

            then(updateSubscriptionUseCase).should().update(any());
        }

        @Test
        @DisplayName("지원하지 않는 국가 코드가 포함되면 400을 반환한다")
        void update_unsupportedCountryCode_returns400() throws Exception {
            // given
            UpdateSubscriptionRequest request = new UpdateSubscriptionRequest(List.of("KR", "XX"));
            given(updateSubscriptionUseCase.update(any()))
                    .willThrow(new EcoSyncException(ErrorCode.UNSUPPORTED_COUNTRY));

            // when & then
            mockMvc.perform(put("/api/subscriptions/{id}", ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDataEncoder.encode(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("SUBSCRIPTION_003"));

            then(updateSubscriptionUseCase).should().update(any());
        }
    }

    @Nested
    @DisplayName("GET /api/subscriptions — 구독 재조회")
    class GetByEmail {

        private static final String EMAIL = "test@example.com";
        private static final List<String> COUNTRY_CODES = List.of("KR", "US");
        private static final String CALENDAR_URL = "http://localhost:8080/api/calendar/token-uuid/subscribe";

        @Test
        @DisplayName("유효한 이메일이면 200과 구독 정보를 반환한다")
        void getByEmail_success() throws Exception {
            // given
            given(getSubscriptionUseCase.getByEmail(any())).willReturn(
                    new GetSubscriptionUseCase.Result(1L, EMAIL, COUNTRY_CODES, CALENDAR_URL));

            // when & then
            mockMvc.perform(get("/api/subscriptions").param("email", EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.countryCodes").isArray())
                    .andExpect(jsonPath("$.calendarUrl").value(CALENDAR_URL));

            then(getSubscriptionUseCase).should().getByEmail(new GetSubscriptionUseCase.Query(EMAIL));
        }

        @Test
        @DisplayName("email 파라미터가 없으면 400을 반환한다")
        void getByEmail_missingParam_returns400() throws Exception {
            // when & then
            mockMvc.perform(get("/api/subscriptions"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_001"));

            then(getSubscriptionUseCase).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("이메일 형식이 올바르지 않으면 400을 반환한다")
        void getByEmail_invalidEmail_returns400() throws Exception {
            // when & then
            mockMvc.perform(get("/api/subscriptions").param("email", "not-an-email"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("COMMON_001"));

            then(getSubscriptionUseCase).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("구독이 없으면 404를 반환한다")
        void getByEmail_notFound_returns404() throws Exception {
            // given
            given(getSubscriptionUseCase.getByEmail(any()))
                    .willThrow(new EcoSyncException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/subscriptions").param("email", EMAIL))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("SUBSCRIPTION_001"));

            then(getSubscriptionUseCase).should().getByEmail(any());
        }
    }
}
