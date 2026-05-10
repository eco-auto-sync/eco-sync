package com.ecosync.api.controller;

import com.ecosync.api.swagger.CalendarApi;
import com.ecosync.application.port.in.GetCalendarUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController implements CalendarApi {

    private final GetCalendarUseCase getCalendarUseCase;

    @Override
    @GetMapping("/{token}/subscribe")
    public ResponseEntity<byte[]> subscribe(@PathVariable String token) {
        byte[] icsContent = getCalendarUseCase.getCalendar(token).icsContent();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"calendar.ics\"")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(icsContent);
    }
}
