package com.ecosync.application.port.in;

import java.util.List;

public interface CreateSubscriptionUseCase {

    record Command(String email, List<String> countryCodes) {}

    record Result(Long id, String calendarUrl) {}

    Result create(Command command);
}
