package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.service.LectureSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(produces = "application/json")
public class RoomController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final LectureSyncService syncService;

    public RoomController(LectureSyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/sync-lectures")
    public String syncLectures() {
        syncService.syncAllRooms();
        return "Sync completed successfully!";
    }

    @GetMapping("/rooms")
    public List<Map<String, Object>> getAllRooms() {
        String sql = "SELECT * FROM rooms";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows;
    }
}