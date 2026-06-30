package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.CancelOccupancyResponseDto;
import ge.freeroom.freeroom.dto.ReserveRoomResponseDto;
import ge.freeroom.freeroom.entities.NotificationPreference;
import ge.freeroom.freeroom.entities.Room;
import ge.freeroom.freeroom.entities.RoomOccupancy;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.LectureRepository;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.repositories.RoomRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
public class RoomAvailabilityServiceTest {

    @Mock
    private RoomOccupancyRepository roomOccupancyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private LectureRepository lectureRepository;

    @Mock
    private TimeService timeService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RoomAvailabilityService roomAvailabilityService;

    private RoomOccupancy validOccupancy;

    @BeforeEach
    void setUp() {
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

        lenient().when(timeService.now()).thenReturn(LocalDateTime.now());
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

    @Test
    void bookingBlockedWhenPreferenceIsNone() {
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.NONE);
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));
        assertThrows(IllegalStateException.class, () -> roomAvailabilityService.reserveRoom("uid", 1L, 60L));

        verify(emailService, never()).sendReservationConfirmation(any(), anyInt(), any());
    }

    @Test
    void bookingBlockedWhenTelegramSelectedButNotLinked() {
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.TELEGRAM);
        user.setTelegramChatId(null);
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));
        assertThrows(IllegalStateException.class, () -> roomAvailabilityService.reserveRoom("uid", 1L, 60L));
    }

    @Test
    void bookingAllowedWhenEmailSelected() {
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.EMAIL);
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(101);

        // Fixed: Updated to match the pessimistic lock query method used in the service
        when(roomRepository.findByIdWithLock(1L)).thenReturn(Optional.of(room));

        when(roomOccupancyRepository.findActiveOccupancyByUserId(eq("uid"), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(lectureRepository.findActiveLecturesByRoomIds(any(), any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(eq(1L))).thenReturn(Optional.empty());

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(LocalDateTime.now());
        saved.setExpectedEndAt(LocalDateTime.now().plusMinutes(60));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        ReserveRoomResponseDto result = roomAvailabilityService.reserveRoom("uid", 1L, 60L);
        assertNotNull(result);
    }

    @Test
    void emailSentOnReservationWhenPreferenceIsEmail() {
        User user = new User();
        user.setId("uid");
        user.setEmail("test@freeuni.edu.ge");
        user.setNotificationPreference(NotificationPreference.EMAIL);
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(101);
        when(roomRepository.findByIdWithLock(1L)).thenReturn(Optional.of(room));
        when(roomOccupancyRepository.findActiveOccupancyByUserId(eq("uid"), any())).thenReturn(Optional.empty());
        when(lectureRepository.findActiveLecturesByRoomIds(any(), any())).thenReturn(Collections.emptyList());
        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(1L)).thenReturn(Optional.empty());

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(LocalDateTime.now());
        saved.setExpectedEndAt(LocalDateTime.now().plusMinutes(60));
        when(roomOccupancyRepository.save(any())).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, 60L);

        verify(emailService, times(1)).sendReservationConfirmation(
                eq("test@freeuni.edu.ge"),
                eq(101),
                any(LocalDateTime.class)
        );
    }

    @Test
    void emailNotSentOnReservationWhenPreferenceIsTelegram() {
        User user = new User();
        user.setId("uid");
        user.setEmail("test@freeuni.edu.ge");
        user.setNotificationPreference(NotificationPreference.TELEGRAM);
        user.setTelegramChatId(123456L);
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(101);
        when(roomRepository.findByIdWithLock(1L)).thenReturn(Optional.of(room));
        when(roomOccupancyRepository.findActiveOccupancyByUserId(eq("uid"), any())).thenReturn(Optional.empty());
        when(lectureRepository.findActiveLecturesByRoomIds(any(), any())).thenReturn(Collections.emptyList());
        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(1L)).thenReturn(Optional.empty());

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(LocalDateTime.now());
        saved.setExpectedEndAt(LocalDateTime.now().plusMinutes(60));
        when(roomOccupancyRepository.save(any())).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, 60L);

        verify(emailService, never()).sendReservationConfirmation(any(), anyInt(), any());
    }

    private void stubHappyPathReservation(User user, Room room, LocalDateTime fixedNow) {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roomRepository.findByIdWithLock(room.getId())).thenReturn(Optional.of(room));
        when(roomOccupancyRepository.findActiveOccupancyByUserId(eq(user.getId()), any())).thenReturn(Optional.empty());
        when(lectureRepository.findActiveLecturesByRoomIds(any(), any())).thenReturn(Collections.emptyList());
        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(room.getId())).thenReturn(Optional.empty());
        when(timeService.now()).thenReturn(fixedNow);
    }

    @Test
    void reserveRoom_negativeDuration_clampedToOneMinute() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 1, 12, 0);
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.EMAIL);
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(101);

        stubHappyPathReservation(user, room, fixedNow);

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(fixedNow);
        saved.setExpectedEndAt(fixedNow.plusMinutes(1));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, -10L);

        ArgumentCaptor<RoomOccupancy> captor = ArgumentCaptor.forClass(RoomOccupancy.class);
        verify(roomOccupancyRepository).save(captor.capture());
        assertEquals(fixedNow.plusMinutes(1), captor.getValue().getExpectedEndAt());
    }

    @Test
    void reserveRoom_durationAbove480_clampedTo480() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 1, 12, 0);
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.EMAIL);
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(101);

        stubHappyPathReservation(user, room, fixedNow);

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(fixedNow);
        saved.setExpectedEndAt(fixedNow.plusMinutes(480));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, 9999L);

        ArgumentCaptor<RoomOccupancy> captor = ArgumentCaptor.forClass(RoomOccupancy.class);
        verify(roomOccupancyRepository).save(captor.capture());
        assertEquals(fixedNow.plusMinutes(480), captor.getValue().getExpectedEndAt());
    }

    @Test
    void reserveRoom_nullDuration_defaultsTo60() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 1, 12, 0);
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.EMAIL);
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(101);

        stubHappyPathReservation(user, room, fixedNow);

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(fixedNow);
        saved.setExpectedEndAt(fixedNow.plusMinutes(60));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, null);

        ArgumentCaptor<RoomOccupancy> captor = ArgumentCaptor.forClass(RoomOccupancy.class);
        verify(roomOccupancyRepository).save(captor.capture());
        assertEquals(fixedNow.plusMinutes(60), captor.getValue().getExpectedEndAt());
    }
}