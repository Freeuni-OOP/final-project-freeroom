package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.CancelOccupancyResponseDto;
import ge.freeroom.freeroom.entities.Room;
import ge.freeroom.freeroom.entities.RoomOccupancy;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomAvailabilityServiceTest {

    @Mock
    private RoomOccupancyRepository roomOccupancyRepository;

    private RoomAvailabilityService roomAvailabilityService;

    private RoomOccupancy validOccupancy;

    @BeforeEach
    void setUp() {
        roomAvailabilityService = new RoomAvailabilityService(
                null, null, null, roomOccupancyRepository
        );

        User validUser = new User();
        validUser.setId("user123");

        Room validRoom = new Room();
        validRoom.setId(1L);
        validRoom.setRoomNumber(404);

        validOccupancy = new RoomOccupancy();
        validOccupancy.setId(10L);
        validOccupancy.setUser(validUser);
        validOccupancy.setRoom(validRoom);
        validOccupancy.setStartAt(LocalDateTime.now().minusMinutes(10));
        validOccupancy.setExpectedEndAt(LocalDateTime.now().plusMinutes(50));
        validOccupancy.setEndAt(null);
    }

    @Test
    void cancelOccupancy_Success_WhenUserOwnsOccupancy() {
        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(1L))
                .thenReturn(Optional.of(validOccupancy));

        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(validOccupancy);

        CancelOccupancyResponseDto response = roomAvailabilityService.cancelOccupancy("user123", 1L);

        assertNotNull(response);
        assertEquals(1L, response.getRoomId());
        assertEquals(404, response.getRoomNumber());
        assertEquals("Occupancy cancelled successfully", response.getMessage());
        assertNotNull(validOccupancy.getEndAt());

        verify(roomOccupancyRepository, times(1)).save(validOccupancy);
    }

    @Test
    void cancelOccupancy_ThrowsException_WhenNoActiveOccupancy() {
        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(1L))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            roomAvailabilityService.cancelOccupancy("user123", 1L);
        });

        assertEquals("No active occupancy for this room", exception.getMessage());

        verify(roomOccupancyRepository, never()).save(any());
    }

    @Test
    void cancelOccupancy_ThrowsAccessDenied_WhenUserDoesNotOwnOccupancy() {
        User maliciousUser = new User();
        maliciousUser.setId("hackerKala");

        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(1L))
                .thenReturn(Optional.of(validOccupancy));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            roomAvailabilityService.cancelOccupancy("hackerKala", 1L);
        });

        assertEquals("You can only cancel your own occupancy", exception.getMessage());
        assertNull(validOccupancy.getEndAt());

        verify(roomOccupancyRepository, never()).save(any());
    }
}
