package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.entities.NotificationPreference;
import ge.freeroom.freeroom.entities.Room;
import ge.freeroom.freeroom.entities.RoomOccupancy;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationSchedulerTest {

    @Mock
    private RoomOccupancyRepository roomOccupancyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TelegramBotService telegramBotService;

    @Mock
    private TimeService timeService;

    @InjectMocks
    private NotificationScheduler scheduler;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        lenient().when(timeService.now()).thenReturn(java.time.LocalDateTime.now());
    }

    private RoomOccupancy buildReservation(User user) {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(101);
        RoomOccupancy r = new RoomOccupancy();
        r.setId(1L);
        r.setRoom(room);
        r.setUser(user);
        r.setNotifiedTenMin(false);
        return r;
    }

    @Test
    void telegramUserGetsNotifiedAndFlagSet() {
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.TELEGRAM);
        user.setTelegramChatId(12345L);
        RoomOccupancy reservation = buildReservation(user);
        when(roomOccupancyRepository.findReservationsNeedingNotification(any(), any())).thenReturn(List.of(reservation));
        when(telegramBotService.sendNotification(eq(12345L), anyString())).thenReturn(SendResult.SUCCESS);

        scheduler.sendExpiryWarnings();

        verify(telegramBotService, times(1)).sendNotification(eq(12345L), anyString());
        verify(roomOccupancyRepository, times(1)).save(reservation);
        assertTrue(reservation.isNotifiedTenMin());
    }

    @Test
    void blockedUserGetsUnlinked() {
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.TELEGRAM);
        user.setTelegramChatId(12345L);
        RoomOccupancy reservation = buildReservation(user);
        when(roomOccupancyRepository.findReservationsNeedingNotification(any(), any())).thenReturn(List.of(reservation));
        when(telegramBotService.sendNotification(eq(12345L), anyString())).thenReturn(SendResult.BLOCKED);

        scheduler.sendExpiryWarnings();

        verify(userRepository, times(1)).save(user);
        assertNull(user.getTelegramChatId());
        assertEquals(NotificationPreference.NONE, user.getNotificationPreference());
        assertFalse(reservation.isNotifiedTenMin());
    }

    @Test
    void transientFailureDoesNotUnlinkOrMarkNotified() {
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.TELEGRAM);
        user.setTelegramChatId(12345L);
        RoomOccupancy reservation = buildReservation(user);
        when(roomOccupancyRepository.findReservationsNeedingNotification(any(), any())).thenReturn(List.of(reservation));
        when(telegramBotService.sendNotification(eq(12345L), anyString())).thenReturn(SendResult.OTHER_ERROR);

        scheduler.sendExpiryWarnings();

        verify(userRepository, never()).save(any());
        verify(roomOccupancyRepository, never()).save(any());
        assertFalse(reservation.isNotifiedTenMin());
        assertEquals(12345L, user.getTelegramChatId());
        assertEquals(NotificationPreference.TELEGRAM, user.getNotificationPreference());
    }

    @Test
    void emailUserMarkedNotifiedWithoutTelegram() {
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.EMAIL);
        RoomOccupancy reservation = buildReservation(user);
        when(roomOccupancyRepository.findReservationsNeedingNotification(any(), any())).thenReturn(List.of(reservation));

        scheduler.sendExpiryWarnings();

        verify(telegramBotService, never()).sendNotification(anyLong(), anyString());
        verify(roomOccupancyRepository, times(1)).save(reservation);
        assertTrue(reservation.isNotifiedTenMin());
    }

    @Test
    void noReservationsDoesNothing() {
        when(roomOccupancyRepository.findReservationsNeedingNotification(any(), any())).thenReturn(Collections.emptyList());

        scheduler.sendExpiryWarnings();

        verify(telegramBotService, never()).sendNotification(anyLong(), anyString());
        verify(roomOccupancyRepository, never()).save(any());
    }
}
