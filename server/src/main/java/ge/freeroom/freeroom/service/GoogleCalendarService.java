package ge.freeroom.freeroom.service;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import ge.freeroom.freeroom.model.CalendarEvent;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoogleCalendarService {
    private static final String API_CALL_FIELDS = "items(id,summary,start,end,description,location)";
    private final Calendar googleCalendarClient;
    private Map<String, String> roomCalendars;     // roomName --> calendarID

    public GoogleCalendarService(Calendar googleCalendarClient) {
        this.googleCalendarClient = googleCalendarClient;
        roomCalendars = new HashMap<>();
    }


    /*
     *      load roomName -> calendarID at startup
     */
    @PostConstruct
    public void loadRoomCalendars() {
        try {
            List<CalendarListEntry> calendars = googleCalendarClient
                    .calendarList()
                    .list()
                    .execute()
                    .getItems();

            roomCalendars = calendars.stream()
                    .filter(c -> c.getSummary().startsWith("Room"))
                    .collect(Collectors.toMap(
                            CalendarListEntry::getSummary,
                            CalendarListEntry::getId
                    ));
        } catch (IOException e) {
            System.out.println("----- [ERROR] : Could not load room calendars!");
            e.printStackTrace();
        }
    }

    /*
            returns all events presented in a specific room as List<CalendarEvent>
             * if invalid room is passed in the function returns null;
     */
    public List<CalendarEvent> getAllEvents(String roomName) {
        String calendarId = roomCalendars.get(roomName);
        if(calendarId == null)
            throw new IllegalArgumentException("No room calendar found with name " + roomName);

        try {
            List<Event> googleEvents = googleCalendarClient
                    .events()
                    .list(calendarId)
                    .setFields(API_CALL_FIELDS)
                    .execute()
                    .getItems();

            List<CalendarEvent> res = new ArrayList<>();
            for(Event event : googleEvents) {
                CalendarEvent calendarEvent = new CalendarEvent(event);
                res.add(calendarEvent);
            }

            return res;
        }catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("--- Failed to fetch events for room: " + roomName, e);
        }
    }
}
