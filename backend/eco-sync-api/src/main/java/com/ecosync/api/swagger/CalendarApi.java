package com.ecosync.api.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Calendar", description = "ICS 캘린더 구독")
public interface CalendarApi {

    @Operation(
            summary = "ICS 파일 스트리밍",
            description = "캘린더 토큰으로 ICS 파일을 스트리밍합니다. Google Calendar 등 외부 캘린더에서 구독 URL로 등록하여 사용합니다."
    )
    ResponseEntity<byte[]> subscribe(
            @Parameter(description = "캘린더 구독 토큰 (UUID)", example = "abc123-uuid-xxxx") String token);
}
