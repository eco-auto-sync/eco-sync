package com.ecosync.infrastructure.ics;

import com.ecosync.application.exception.EcoSyncException;
import com.ecosync.application.exception.ErrorCode;
import com.ecosync.application.port.out.IcsGeneratorPort;
import com.ecosync.domain.event.EconomicEvent;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class IcsGeneratorAdapter implements IcsGeneratorPort {

    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    @Override
    public byte[] generate(List<EconomicEvent> events) {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//EcoSync//EcoSync Calendar 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        for (EconomicEvent event : events) {
            try {
                Date date = new Date(event.getEventDate().format(BASIC_DATE));
                VEvent vEvent = new VEvent(date, event.getTitle());
                vEvent.getProperties().add(new Uid(event.getUid()));
                calendar.getComponents().add(vEvent);
            } catch (Exception e) {
                throw new EcoSyncException(ErrorCode.CALENDAR_GENERATION_FAILED);
            }
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new CalendarOutputter().output(calendar, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new EcoSyncException(ErrorCode.CALENDAR_GENERATION_FAILED);
        }
    }
}
