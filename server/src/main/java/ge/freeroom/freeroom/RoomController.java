package ge.freeroom.freeroom;

import ge.freeroom.freeroom.model.CalendarEvent;
import ge.freeroom.freeroom.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RoomController {

    // THIS IS JUST FOR TESTING DB CONNECTION, WILL BE REMOVED
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // FOR TESTING GOOGLE CALENDAR API
    private final GoogleCalendarService calendarService;

    public RoomController(GoogleCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/calendar/rooms")
    public List<String> getRooms() {
        return calendarService.getAllRooms();
    }

    @GetMapping("/rooms/{roomName}/events")
    public List<CalendarEvent> getEvents(@PathVariable String roomName) {
        return calendarService.getAllEventsForRoom(roomName);
    }

    @GetMapping("/rooms")
    public List<Map<String, Object>> getAllRooms() {
        String sql = "SELECT * FROM rooms";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows;
    }
}