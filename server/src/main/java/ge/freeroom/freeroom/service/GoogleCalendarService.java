package ge.freeroom.freeroom.service;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.client.util.DateTime;
import ge.freeroom.freeroom.model.CalendarEvent;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                    .filter(c -> c.getId().toLowerCase().contains("resource.calendar.google.com")
                            || c.getId().toLowerCase().startsWith("room"))
                    .collect(Collectors.toMap(
                            c -> extractRoomName(c.getSummary(), c.getId()),
                            CalendarListEntry::getId,
                            (existing, replacement) -> existing // on duplicate --> first one stays. ?? MARK:: ra vqnat aqq? anu 308-ze qvemot extract-ze orive tipis meilia da maset conflictze ravqnat.. an gavarkviot marto room308@freeuni.edu.ge tipis meilebi xoar davitovot vafshee is meore risia idkk
                    ));
            for (CalendarListEntry entry : calendars) {
                System.out.println("summary=" + entry.getSummary()
                        + " | override=" + entry.getSummaryOverride()
                        + " | id=" + entry.getId());
            }
            System.out.println("---- Loaded rooms: " + roomCalendars.keySet().toString());
        } catch (IOException e) {
            System.out.println("----- [ERROR] : Could not load room calendars!");
            e.printStackTrace();
        }
    }

    public List<String> getAllRooms() {
        return new ArrayList<>(roomCalendars.keySet());
    }

    public List<String> getRoomsByFloor(int floor) {
        return roomCalendars.keySet().stream()
                .filter(room -> room.toLowerCase().startsWith("room " + floor))
                .toList();
    }

    /*
     * returns all events presented in a specific room as List<CalendarEvent>
     * if invalid room is passed in the function returns null;
     */
    public List<CalendarEvent> getAllEventsForRoom(String roomName) {
        String calendarId = roomCalendars.get(roomName);
        if(calendarId == null) {
            throw new IllegalArgumentException("No room calendar found with name " + roomName);
        }

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

            System.out.println("---- Loaded rooms: " + res.toString());
            return res;
        }catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("--- Failed to fetch events for room: " + roomName, e);
        }
    }

    /*
     * takes in room name, google.client.util's DateTime structure for date min and max.
     * --> returns all events specified in the time range
     */
    public List<CalendarEvent> getEventsForRoomRange(String roomName, LocalDateTime start, LocalDateTime end) {
        String calendarId = roomCalendars.get(roomName);
        if(calendarId == null) {
            throw new IllegalArgumentException("No room calendar found with name " + roomName);
        }

        try {
            List<Event> googleEvents = googleCalendarClient
                    .events()
                    .list(calendarId)
                    .setFields(API_CALL_FIELDS)
                    .setTimeMin(toGoogleDateTime(start))
                    .setTimeMax(toGoogleDateTime(end))
                    .execute()
                    .getItems();

            List<CalendarEvent> res = new ArrayList<>();
            for(Event event : googleEvents) {
                CalendarEvent calendarEvent = new CalendarEvent(event);
                res.add(calendarEvent);
            }

            return res;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("--- Failed to fetch events for room: " + roomName, e);
        }
    }

    // returns true if the specified room is empty in given timeframe
    public boolean isRoomFree(String roomName, LocalDateTime start, LocalDateTime end) {
        return getEventsForRoomRange(roomName, start, end).isEmpty();
    }

    public List<String> getFreeRooms(LocalDateTime start, LocalDateTime end) {
        return roomCalendars.keySet().parallelStream()
                .filter(room -> isRoomFree(room, start, end))
                .collect(Collectors.toList());
    }

    // casts LocalDateTime into Google's Calendar API requested type
    private DateTime toGoogleDateTime(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return new DateTime(instant.toEpochMilli());
    }

    // gets "Room 423" from 2 different types of room getSummary mails
    private String extractRoomName(String summary, String id) {
        // type 1: extract digits from "room423@..." pattern --> matches id(also mail)
        Matcher m = Pattern.compile("room(\\d+)@").matcher(id);
        if (m.find()) {
            return "Room " + m.group(1);
        }

        // type 2: "მთავარი კორპუსი-3-ოთახი #308 (20)" --> "Room 308"
        m = Pattern.compile("#(\\d+(-\\d+)?)").matcher(summary);
        if (m.find()) {
            return "Room " + m.group(1);
        }

        // if nothin else then use summary as is (as usual those two types as a whole if nothing else comes up later)
        return summary;
    }
}
