package ge.freeroom.freeroom.model;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class CalendarEvent {
    private String id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private String location;

    public CalendarEvent(Event event) {
        id = event.getId();
        title = event.getSummary();
        startTime = toLocalDateTime(event.getStart());
        endTime = toLocalDateTime(event.getEnd());
        description = event.getDescription();
        location = event.getLocation();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    private LocalDateTime toLocalDateTime(EventDateTime eventDateTime) {
        if(eventDateTime == null) return null;

        if (eventDateTime.getDateTime() != null) { // timed event (has specific time)
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(eventDateTime.getDateTime().getValue()),
                    ZoneId.systemDefault()
            );
        } else { // all-day event (only has date, no time)
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(eventDateTime.getDate().getValue()),
                    ZoneId.systemDefault()
            );
        }
    }
}
