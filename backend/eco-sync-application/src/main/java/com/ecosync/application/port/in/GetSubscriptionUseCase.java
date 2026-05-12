package com.ecosync.application.port.in;

import java.util.List;

public interface GetSubscriptionUseCase {

    record Query(String email) {}

    record Result(Long id, String email, List<String> countryCodes, String calendarUrl) {}

    Result getByEmail(Query query);
}
