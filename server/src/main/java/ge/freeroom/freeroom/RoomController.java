package ge.freeroom.freeroom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RoomController {

    // THIS IS JUST FOR TESTING DB CONNECTION, WILL BE REMOVED
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/rooms")
    public List<Map<String, Object>> getAllRooms() {
        String sql = "SELECT * FROM rooms";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows;
    }
}