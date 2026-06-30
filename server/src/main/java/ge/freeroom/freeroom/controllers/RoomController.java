package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.CancelOccupancyResponseDto;
import ge.freeroom.freeroom.dto.ReserveRoomRequestDto;
import ge.freeroom.freeroom.dto.ReserveRoomResponseDto;
import ge.freeroom.freeroom.dto.RoomMapDto;
import ge.freeroom.freeroom.service.LectureSyncService;
import ge.freeroom.freeroom.service.RoomAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping(produces = "application/json")
public class RoomController {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Autowired
    private RoomAvailabilityService roomAvailabilityService;

    public RoomController() {
    }

    @GetMapping("/rooms")
    public List<Map<String, Object>> getAllRooms() {
        String sql = "SELECT * FROM room";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows;
    }

    @GetMapping("/rooms/map")
    public List<RoomMapDto> getRoomMap(Principal principal){
        String userId = principal.getName();
        return roomAvailabilityService.getAllRoomsMap(userId);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveRoomResponseDto> addRoom(@Valid @RequestBody ReserveRoomRequestDto request, Principal principal) {
        String userId = principal.getName();
        Long roomId = request.getRoomDbId();
        Integer roomNumber = request.getRoomNumber();
        Long durationMinutes = request.getDurationMinutes();

        if (roomId == null) {
            if (roomNumber == null) {
                return ResponseEntity.badRequest().build();
            }
            roomId = roomNumber.longValue();
        }

        ReserveRoomResponseDto response = roomAvailabilityService.reserveRoom(userId, roomId, durationMinutes);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/rooms/{roomId}/cancel")
    public ResponseEntity<CancelOccupancyResponseDto> cancelOccupancy(
            @PathVariable Long roomId,
            Principal principal) {
        String userId = principal.getName();
        CancelOccupancyResponseDto response = roomAvailabilityService.cancelOccupancy(userId, roomId);
        return ResponseEntity.ok(response);
    }
}