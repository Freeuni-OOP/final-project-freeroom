package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.CancelOccupancyResponseDto;
import ge.freeroom.freeroom.dto.ReserveRoomRequestDto;
import ge.freeroom.freeroom.dto.ReserveRoomResponseDto;
import ge.freeroom.freeroom.dto.RoomMapDto;
import ge.freeroom.freeroom.dto.UpdatePublicNoteRequestDto;
import ge.freeroom.freeroom.security.RateLimiter;
import ge.freeroom.freeroom.service.RoomAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(produces = "application/json")
public class RoomController {

    private final RoomAvailabilityService roomAvailabilityService;
    private final RateLimiter rateLimiter;

    public RoomController(RoomAvailabilityService roomAvailabilityService, RateLimiter rateLimiter) {
        this.roomAvailabilityService = roomAvailabilityService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/rooms/map")
    public List<RoomMapDto> getRoomMap(Principal principal){
        String userId = principal.getName();
        return roomAvailabilityService.getAllRoomsMap(userId);
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveRoomResponseDto> addRoom(@Valid @RequestBody ReserveRoomRequestDto request, Principal principal) {
        if (!rateLimiter.allow("reserve:" + principal.getName(), 5, 60000)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
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

        String publicNote = request.getPublicNote();
        if (publicNote != null) {
            publicNote = Jsoup.clean(publicNote, Safelist.none());
        }

        ReserveRoomResponseDto response = roomAvailabilityService.reserveRoom(userId, roomId, durationMinutes, publicNote);
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

    @PatchMapping("/rooms/{roomId}/note")
    public ResponseEntity<Void> updatePublicNote(
            @PathVariable Long roomId,
            @Valid @RequestBody UpdatePublicNoteRequestDto request,
            Principal principal) {
        String userId = principal.getName();
        String sanitizedNote = request.getPublicNote() != null ? Jsoup.clean(request.getPublicNote(), Safelist.none()) : null;
        roomAvailabilityService.updatePublicNote(userId, roomId, sanitizedNote);
        return ResponseEntity.ok().build();
    }
}