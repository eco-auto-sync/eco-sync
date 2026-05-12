package com.ecosync.api.controller;

import com.ecosync.api.config.SecurityConfig;
import com.ecosync.application.exception.EcoSyncException;
import com.ecosync.application.exception.ErrorCode;
import com.ecosync.application.port.in.GetCalendarUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CalendarController.class)
@Import(SecurityConfig.class)
class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetCalendarUseCase getCalendarUseCase;

    @Nested
    @DisplayName("GET /api/calendar/{token}/subscribe — ICS 구독")
    class Subscribe {

        private static final String TOKEN = "test-token-uuid";

        @Test
        @DisplayName("유효한 토큰이면 200과 ICS 파일을 반환한다")
        void subscribe_success() throws Exception {
            // given
            byte[] icsBytes = "BEGIN:VCALENDAR\r\nEND:VCALENDAR".getBytes();
            given(getCalendarUseCase.getCalendar(TOKEN)).willReturn(new GetCalendarUseCase.Result(icsBytes));

            // when & then
            mockMvc.perform(get("/api/calendar/{token}/subscribe", TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, containsString("text/calendar")))
                    .andExpect(content().bytes(icsBytes));

            then(getCalendarUseCase).should().getCalendar(TOKEN);
        }

        @Test
        @DisplayName("토큰에 해당하는 구독이 없으면 404를 반환한다")
        void subscribe_notFound_returns404() throws Exception {
            // given
            given(getCalendarUseCase.getCalendar(TOKEN))
                    .willThrow(new EcoSyncException(ErrorCode.CALENDAR_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/calendar/{token}/subscribe", TOKEN))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("CALENDAR_001"));

            then(getCalendarUseCase).should().getCalendar(TOKEN);
        }
    }
}
