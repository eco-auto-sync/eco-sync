package com.ecosync.application.port.in;

public interface GetCalendarUseCase {

    record Result(byte[] icsContent) {}

    Result getCalendar(String token);
}
