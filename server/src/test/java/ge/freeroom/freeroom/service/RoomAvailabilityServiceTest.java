package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.CancelOccupancyResponseDto;
import ge.freeroom.freeroom.dto.ReserveRoomResponseDto;
import ge.freeroom.freeroom.entities.*;
import ge.freeroom.freeroom.repositories.FriendshipRepository;
import ge.freeroom.freeroom.repositories.LectureRepository;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.repositories.RoomRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.websocket.RealtimeEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceTest {

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

    @Mock
    private ChatService chatService;

    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private RealtimeEventPublisher realtimeEventPublisher;

    @InjectMocks
    private RoomAvailabilityService roomAvailabilityService;

    @BeforeEach
    void setUp() {
        lenient().when(friendshipRepository.findFriendIdsByUserId(anyString())).thenReturn(Collections.emptyList());
        lenient().doNothing().when(realtimeEventPublisher).publishRoomEvent(any());
        lenient().doNothing().when(realtimeEventPublisher).publishOccupancyRipple(anyString(), anyList());
        lenient().doNothing().when(chatService).initializeRoomBooker(anyLong(), anyString());
        lenient().doNothing().when(chatService).clearRoomChat(anyLong());
    }

    private User emailUser() {
        User user = new User();
        user.setId("uid");
        user.setEmail("test@freeuni.edu.ge");
        user.setNotificationPreference(NotificationPreference.EMAIL);
        return user;
    }

    private Room basicRoom() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(101);

        Floor floor = new Floor();
        floor.setId(1L);
        floor.setNumber(1);
        room.setFloor(floor);

        return room;
    }

    private void stubRoomFreeAndAvailable(Room room, String userId) {
        lenient().when(roomRepository.findByIdWithLock(room.getId())).thenReturn(Optional.of(room));
        lenient().when(roomOccupancyRepository.findActiveOccupancyByUserId(eq(userId), any())).thenReturn(Optional.empty());
        lenient().when(lectureRepository.findActiveLecturesByRoomIds(anyList(), any())).thenReturn(Collections.emptyList());
        lenient().when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(room.getId())).thenReturn(Optional.empty());
        lenient().when(lectureRepository.findNextLecturesByRoomId(eq(room.getId()), any())).thenReturn(Collections.emptyList());
    }

    @Test
    void cancelOccupancy_Success_WhenUserOwnsOccupancy() {
        User validUser = new User();
        validUser.setId("user123");

        Room validRoom = new Room();
        validRoom.setId(1L);
        validRoom.setRoomNumber(404);
        Floor floor = new Floor();
        floor.setId(1L);
        floor.setNumber(4);
        validRoom.setFloor(floor);

        RoomOccupancy validOccupancy = new RoomOccupancy();
        validOccupancy.setId(10L);
        validOccupancy.setUser(validUser);
        validOccupancy.setRoom(validRoom);
        validOccupancy.setStartAt(LocalDateTime.now().minusMinutes(10));
        validOccupancy.setExpectedEndAt(LocalDateTime.now().plusMinutes(50));
        validOccupancy.setEndAt(null);

        when(timeService.now()).thenReturn(LocalDateTime.of(2026, 7, 6, 12, 0));
        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(1L))
                .thenReturn(Optional.of(validOccupancy));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(validOccupancy);
        when(friendshipRepository.findFriendIdsByUserId("user123")).thenReturn(Collections.emptyList());

        CancelOccupancyResponseDto response = roomAvailabilityService.cancelOccupancy("user123", 1L);

        assertNotNull(response);
        assertEquals(1L, response.getRoomId());
        assertEquals(404, response.getRoomNumber());
        assertEquals("Occupancy cancelled successfully", response.getMessage());
        assertNotNull(validOccupancy.getEndAt());

        verify(roomOccupancyRepository, times(1)).save(validOccupancy);
        verify(chatService, never()).clearRoomChat(anyLong());
        verify(realtimeEventPublisher).publishOccupancyRipple(eq("user123"), anyList());
    }

    @Test
    void cancelOccupancy_ThrowsException_WhenNoActiveOccupancy() {
        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(1L))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                roomAvailabilityService.cancelOccupancy("user123", 1L));

        assertEquals("No active occupancy for this room", exception.getMessage());
        verify(roomOccupancyRepository, never()).save(any());
    }

    @Test
    void cancelOccupancy_ThrowsAccessDenied_WhenUserDoesNotOwnOccupancy() {
        User owner = new User();
        owner.setId("owner123");

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(404);
        Floor floor = new Floor();
        floor.setId(1L);
        floor.setNumber(4);
        room.setFloor(floor);

        RoomOccupancy occupancy = new RoomOccupancy();
        occupancy.setId(20L);
        occupancy.setUser(owner);
        occupancy.setRoom(room);
        occupancy.setStartAt(LocalDateTime.now().minusMinutes(120));
        occupancy.setExpectedEndAt(LocalDateTime.now().plusMinutes(10));
        occupancy.setEndAt(null);

        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(1L))
                .thenReturn(Optional.of(occupancy));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                roomAvailabilityService.cancelOccupancy("differentUser", 1L));

        assertEquals("You can only cancel your own occupancy", exception.getMessage());
        assertNull(occupancy.getEndAt());
        verify(roomOccupancyRepository, never()).save(any());
        verify(chatService, never()).clearRoomChat(anyLong());
    }

    @Test
    void bookingBlockedWhenPreferenceIsNone() {
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.NONE);

        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                roomAvailabilityService.reserveRoom("uid", 1L, 60L, null));

        assertTrue(exception.getMessage().contains("შეტყობინების მეთოდი"));
        verify(emailService, never()).sendReservationConfirmation(any(), anyInt(), any());
    }

    @Test
    void bookingBlockedWhenTelegramSelectedButNotLinked() {
        User user = new User();
        user.setId("uid");
        user.setNotificationPreference(NotificationPreference.TELEGRAM);
        user.setTelegramChatId(null);

        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                roomAvailabilityService.reserveRoom("uid", 1L, 60L, null));

        assertTrue(exception.getMessage().contains("Telegram"));
    }

    @Test
    void bookingAllowedWhenEmailSelected() {
        User user = emailUser();
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        Room room = basicRoom();
        stubRoomFreeAndAvailable(room, "uid");

        LocalDateTime now = LocalDateTime.of(2026, 7, 6, 12, 0);
        when(timeService.now()).thenReturn(now);

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(now);
        saved.setExpectedEndAt(now.plusMinutes(60));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        ReserveRoomResponseDto result = roomAvailabilityService.reserveRoom("uid", 1L, 60L, null);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getRoomId());
        assertEquals(101, result.getRoomNumber());
        assertEquals(now, result.getStartTime());
        assertEquals(now.plusMinutes(60), result.getExpectedEndTime());

        verify(emailService).sendReservationConfirmation(eq("test@freeuni.edu.ge"), eq(101), eq(now.plusMinutes(60)));
    }

    @Test
    void emailSentOnReservationWhenPreferenceIsEmail() {
        User user = emailUser();
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        Room room = basicRoom();
        stubRoomFreeAndAvailable(room, "uid");

        LocalDateTime now = LocalDateTime.of(2026, 7, 6, 12, 0);
        when(timeService.now()).thenReturn(now);

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(now);
        saved.setExpectedEndAt(now.plusMinutes(60));
        when(roomOccupancyRepository.save(any())).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, 60L, null);

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

        Room room = basicRoom();
        stubRoomFreeAndAvailable(room, "uid");

        LocalDateTime now = LocalDateTime.of(2026, 7, 6, 12, 0);
        when(timeService.now()).thenReturn(now);

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(now);
        saved.setExpectedEndAt(now.plusMinutes(60));
        when(roomOccupancyRepository.save(any())).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, 60L, null);

        verify(emailService, never()).sendReservationConfirmation(any(), anyInt(), any());
    }

    @Test
    void reserveRoom_throwsWhenDurationExtendsPastNextLecture() {
        User user = emailUser();
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        Room room = basicRoom();
        stubRoomFreeAndAvailable(room, "uid");

        LocalDateTime now = LocalDateTime.of(2026, 7, 6, 12, 0);
        when(timeService.now()).thenReturn(now);

        Lecture nextLecture = new Lecture();
        nextLecture.setStartAt(now.plusMinutes(45));
        when(lectureRepository.findNextLecturesByRoomId(eq(1L), any())).thenReturn(List.of(nextLecture));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                roomAvailabilityService.reserveRoom("uid", 1L, 60L, null));

        assertTrue(exception.getMessage().contains("45"));
        verify(roomOccupancyRepository, never()).save(any());
    }

    @Test
    void reserveRoom_allowsDurationEndingExactlyAtNextLectureStart() {
        User user = emailUser();
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        Room room = basicRoom();
        stubRoomFreeAndAvailable(room, "uid");

        LocalDateTime now = LocalDateTime.of(2026, 7, 6, 12, 0);
        when(timeService.now()).thenReturn(now);

        Lecture nextLecture = new Lecture();
        nextLecture.setStartAt(now.plusMinutes(45));
        when(lectureRepository.findNextLecturesByRoomId(eq(1L), any())).thenReturn(List.of(nextLecture));

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(now);
        saved.setExpectedEndAt(now.plusMinutes(45));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        ReserveRoomResponseDto result = roomAvailabilityService.reserveRoom("uid", 1L, 45L, null);

        assertNotNull(result);
        assertEquals(now.plusMinutes(45), result.getNextLectureStart());
        assertEquals(45L, result.getMaxAllowedDurationMinutes());
        verify(roomOccupancyRepository, times(1)).save(any(RoomOccupancy.class));
    }

    @Test
    void reserveRoom_allowsDurationWhenNoNextLecture() {
        User user = emailUser();
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        Room room = basicRoom();
        stubRoomFreeAndAvailable(room, "uid");

        LocalDateTime now = LocalDateTime.of(2026, 7, 6, 12, 0);
        when(timeService.now()).thenReturn(now);
        when(lectureRepository.findNextLecturesByRoomId(eq(1L), any())).thenReturn(Collections.emptyList());

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(now);
        saved.setExpectedEndAt(now.plusMinutes(120));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        ReserveRoomResponseDto result = roomAvailabilityService.reserveRoom("uid", 1L, 120L, null);

        assertNotNull(result);
        assertNull(result.getNextLectureStart());
    }

    @Test
    void reserveRoom_populatesNextLectureFieldsOnSuccess() {
        User user = emailUser();
        when(userRepository.findById("uid")).thenReturn(Optional.of(user));

        Room room = basicRoom();
        stubRoomFreeAndAvailable(room, "uid");

        LocalDateTime now = LocalDateTime.of(2026, 7, 6, 12, 0);
        when(timeService.now()).thenReturn(now);

        Lecture nextLecture = new Lecture();
        LocalDateTime lectureStart = now.plusMinutes(90);
        nextLecture.setStartAt(lectureStart);
        when(lectureRepository.findNextLecturesByRoomId(eq(1L), any())).thenReturn(List.of(nextLecture));

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(now);
        saved.setExpectedEndAt(now.plusMinutes(30));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        ReserveRoomResponseDto result = roomAvailabilityService.reserveRoom("uid", 1L, 30L, null);

        assertEquals(lectureStart, result.getNextLectureStart());
        assertEquals(90L, result.getMaxAllowedDurationMinutes());
    }

    @Test
    void reserveRoom_negativeDuration_clampedToOneMinute() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 1, 12, 0);
        User user = emailUser();
        Room room = basicRoom();

        when(userRepository.findById("uid")).thenReturn(Optional.of(user));
        stubRoomFreeAndAvailable(room, "uid");
        when(timeService.now()).thenReturn(fixedNow);

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(fixedNow);
        saved.setExpectedEndAt(fixedNow.plusMinutes(1));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, -10L, null);

        ArgumentCaptor<RoomOccupancy> captor = ArgumentCaptor.forClass(RoomOccupancy.class);
        verify(roomOccupancyRepository).save(captor.capture());
        assertEquals(fixedNow.plusMinutes(1), captor.getValue().getExpectedEndAt());
    }

    @Test
    void reserveRoom_durationAbove480_clampedTo480() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 1, 12, 0);
        User user = emailUser();
        Room room = basicRoom();

        when(userRepository.findById("uid")).thenReturn(Optional.of(user));
        stubRoomFreeAndAvailable(room, "uid");
        when(timeService.now()).thenReturn(fixedNow);

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(fixedNow);
        saved.setExpectedEndAt(fixedNow.plusMinutes(480));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, 9999L, null);

        ArgumentCaptor<RoomOccupancy> captor = ArgumentCaptor.forClass(RoomOccupancy.class);
        verify(roomOccupancyRepository).save(captor.capture());
        assertEquals(fixedNow.plusMinutes(480), captor.getValue().getExpectedEndAt());
    }

    @Test
    void reserveRoom_nullDuration_defaultsTo60() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 1, 12, 0);
        User user = emailUser();
        Room room = basicRoom();

        when(userRepository.findById("uid")).thenReturn(Optional.of(user));
        stubRoomFreeAndAvailable(room, "uid");
        when(timeService.now()).thenReturn(fixedNow);

        RoomOccupancy saved = new RoomOccupancy();
        saved.setId(1L);
        saved.setRoom(room);
        saved.setUser(user);
        saved.setStartAt(fixedNow);
        saved.setExpectedEndAt(fixedNow.plusMinutes(60));
        when(roomOccupancyRepository.save(any(RoomOccupancy.class))).thenReturn(saved);

        roomAvailabilityService.reserveRoom("uid", 1L, null, null);

        ArgumentCaptor<RoomOccupancy> captor = ArgumentCaptor.forClass(RoomOccupancy.class);
        verify(roomOccupancyRepository).save(captor.capture());
        assertEquals(fixedNow.plusMinutes(60), captor.getValue().getExpectedEndAt());
    }

    @Test
    void cancelOccupancy_ThrowsAccessDenied_AndDoesNotClearChat_WhenNonOwnerOnExpiredOccupancy() {
        User owner = new User();
        owner.setId("owner123");

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(404);
        Floor floor = new Floor();
        floor.setId(1L);
        floor.setNumber(4);
        room.setFloor(floor);

        LocalDateTime fixedNow = LocalDateTime.of(2026, 1, 1, 12, 0);
        when(timeService.now()).thenReturn(fixedNow);

        RoomOccupancy expiredOccupancy = new RoomOccupancy();
        expiredOccupancy.setId(20L);
        expiredOccupancy.setUser(owner);
        expiredOccupancy.setRoom(room);
        expiredOccupancy.setStartAt(fixedNow.minusMinutes(120));
        expiredOccupancy.setExpectedEndAt(fixedNow.minusMinutes(10));
        expiredOccupancy.setEndAt(null);

        when(roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(1L))
                .thenReturn(Optional.of(expiredOccupancy));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                roomAvailabilityService.cancelOccupancy("differentUser", 1L));

        assertEquals("You can only cancel your own occupancy", exception.getMessage());
        verify(chatService, never()).clearRoomChat(anyLong());
        verify(roomOccupancyRepository, never()).save(any());
    }
}