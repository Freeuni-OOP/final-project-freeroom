package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.ReserveRoomRequest;
import ge.freeroom.freeroom.dto.ReserveRoomResponse;
import ge.freeroom.freeroom.dto.RoomMapDto;
import ge.freeroom.freeroom.service.LectureSyncService;
import ge.freeroom.freeroom.service.RoomAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.security.Principal;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping(produces = "application/json")
public class RoomController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final LectureSyncService syncService;
    @Autowired
    private RoomAvailabilityService roomAvailabilityService;

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

    @GetMapping("/rooms/map")
    public List<RoomMapDto> getRoomMap(){
        return roomAvailabilityService.getAllRoomsMap();
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveRoomResponse> addRoom(@RequestBody ReserveRoomRequest request, Principal principal) {
        String userId = principal.getName();
        Long roomId = request.getRoomDbId();
        Long durationMinutes = request.getDurationMinutes();

        ReserveRoomResponse response = roomAvailabilityService.reserveRoom(userId, roomId, durationMinutes);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}