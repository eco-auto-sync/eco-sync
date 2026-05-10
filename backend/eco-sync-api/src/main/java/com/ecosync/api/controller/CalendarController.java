package com.ecosync.api.controller;

import com.ecosync.application.port.in.GetCalendarUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Calendar", description = "ICS 캘린더 구독")
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final GetCalendarUseCase getCalendarUseCase;

    @Operation(summary = "ICS 파일 스트리밍")
    @GetMapping("/{token}/subscribe")
    public ResponseEntity<byte[]> subscribe(@PathVariable String token) {
        byte[] icsContent = getCalendarUseCase.getCalendar(token).icsContent();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"calendar.ics\"")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(icsContent);
    }
}
