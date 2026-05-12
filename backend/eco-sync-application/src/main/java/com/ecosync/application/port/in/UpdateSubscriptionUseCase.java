package com.ecosync.application.port.in;

import java.util.List;

public interface UpdateSubscriptionUseCase {

    record Command(Long id, List<String> countryCodes) {}

    record Result(Long id, String calendarUrl) {}

    Result update(Command command);
}
