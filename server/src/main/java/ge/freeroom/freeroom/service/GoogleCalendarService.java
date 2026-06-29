package ge.freeroom.freeroom.service;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.util.DateTime;
import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.entities.Subject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleCalendarService {
    private static final String API_CALL_FIELDS = "items(id,summary,start,end,description,location)";
    private final Calendar googleCalendarClient;

    public GoogleCalendarService(Calendar googleCalendarClient) {
        this.googleCalendarClient = googleCalendarClient;
    }

    public List<Lecture> fetchLectures(int roomNumber, LocalDateTime start, LocalDateTime end) {
        String calendarId = "room" + roomNumber + "@freeuni.edu.ge";
        try {
            List<Event> googleEvents = googleCalendarClient
                    .events()
                    .list(calendarId)
                    .setFields(API_CALL_FIELDS)
                    .setTimeMin(toGoogleDateTime(start))
                    .setTimeMax(toGoogleDateTime(end))
                    .setSingleEvents(true)
                    .setOrderBy("startTime")
                    .execute()
                    .getItems();

            List<Lecture> res = new ArrayList<>();
            if (googleEvents != null) {
                for (Event event : googleEvents) {
                    res.add(mapToLecture(event));
                }
            }
            return res;
        } catch (IOException e) {
            System.err.println("--- Failed to fetch events for room: " + roomNumber);
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private Lecture mapToLecture(Event event) {
        String summary = event.getSummary() != null ? event.getSummary() : "Unknown Title";
        String description = event.getDescription() != null ? event.getDescription() : "";

        Subject transientSubject = LectureSyncService.parseSubject(summary);

        return Lecture.builder()
                .eventExternalId(event.getId())
                .subject(transientSubject)
                .startAt(toLocalDateTime(event.getStart()))
                .endAt(toLocalDateTime(event.getEnd()))
                .recurring(false)
                .fetchedAt(LocalDateTime.now())
                .build();
    }

    private LocalDateTime toLocalDateTime(EventDateTime eventDateTime) {
        if (eventDateTime == null) return null;
        if (eventDateTime.getDateTime() != null) { // timed event
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(eventDateTime.getDateTime().getValue()),
                    ZoneId.systemDefault()
            );
        } else if (eventDateTime.getDate() != null) { // all-day event
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(eventDateTime.getDate().getValue()),
                    ZoneId.systemDefault()
            );
        }
        return null;
    }

    private DateTime toGoogleDateTime(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return new DateTime(instant.toEpochMilli());
    }
}
