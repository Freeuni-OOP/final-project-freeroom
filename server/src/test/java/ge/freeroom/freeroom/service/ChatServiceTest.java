package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.ChatMessageDto;
import ge.freeroom.freeroom.entities.Chat;
import ge.freeroom.freeroom.entities.MessageType;
import ge.freeroom.freeroom.entities.RoomAccess;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.ChatRepository;
import ge.freeroom.freeroom.repositories.RoomAccessRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private RoomAccessRepository roomAccessRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ChatService chatService;

    private static final Pageable PAGEABLE = PageRequest.of(0, 20);

    @Test
    void getMessages_WhenUserIsNonMember_ThrowsSecurityException() {
        Long roomId = 1L;
        String userId = "unauthorized-user";
        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(false);

        assertThrows(SecurityException.class, () -> chatService.getMessages(roomId, null, userId));

        verify(chatRepository, never()).findLatestMessages(any(), any());
    }

    @Test
    void getMessages_WhenBeforeIdIsNull_ReturnsLatestMessages() {
        Long roomId = 1L;
        String userId = "authorized-user";
        ChatMessageDto msg = new ChatMessageDto(10L, "user", "Nick", "placeholder", "t@edu.ge", "Latest", MessageType.TEXT, LocalDateTime.now());
        List<ChatMessageDto> messages = List.of(msg);

        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatRepository.findLatestMessages(eq(roomId), any(Pageable.class))).thenReturn(messages);

        List<ChatMessageDto> result = chatService.getMessages(roomId, null, userId);

        assertEquals(messages, result);
        verify(chatRepository).findLatestMessages(eq(roomId), any(Pageable.class));
    }

    @Test
    void getMessages_WhenUserIsMemberAndBeforeIdProvided_ReturnsOlderMessages() {
        Long roomId = 1L;
        Long beforeId = 50L;
        String userId = "authorized-user";

        ChatMessageDto msg1 = new ChatMessageDto(10L, "u1", "N1", "placeholder1", "e1", "First", MessageType.TEXT, LocalDateTime.now());
        ChatMessageDto msg2 = new ChatMessageDto(11L, "u2", "N2", "placeholder2", "e2", "Second", MessageType.TEXT, LocalDateTime.now());

        List<ChatMessageDto> repoReturned = List.of(msg2, msg1);
        List<ChatMessageDto> expected = List.of(msg1, msg2);

        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatRepository.findOlderMessages(eq(roomId), eq(beforeId), any(Pageable.class))).thenReturn(repoReturned);

        List<ChatMessageDto> result = chatService.getMessages(roomId, beforeId, userId);

        assertEquals(expected, result);
        verify(chatRepository).findOlderMessages(eq(roomId), eq(beforeId), any(Pageable.class));
    }

    @Test
    void sendMessage_WhenUserIsUnauthorized_ThrowsSecurityException() {
        Long roomId = 1L;
        String userId = "attacker-id";
        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(false);

        assertThrows(SecurityException.class, () -> chatService.sendMessage(roomId, userId, "Leak attempt"));
        verify(chatRepository, never()).save(any());
    }

    @Test
    void sendMessage_WhenUserDoesNotExist_ThrowsIllegalArgumentException() {
        Long roomId = 1L;
        String userId = "ghost-user";
        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(roomId, userId, "Test message"));
        verify(chatRepository, never()).save(any());
    }

    @Test
    void sendJoinRequest_SavesRequest() {
        Long roomId = 1L;
        String userId = "valid-requester-id";
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        chatService.sendJoinRequest(roomId, userId);

        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    void approveJoinRequest_WhenApproverIsNotAdmin_ThrowsSecurityException() {
        Long roomId = 1L;
        String adminId = "fake-admin-id";
        String targetUserId = "target-user-id";
        RoomAccess regularAccess = new RoomAccess(roomId, adminId, false);

        when(roomAccessRepository.findByRoomIdAndUserId(roomId, adminId)).thenReturn(Optional.of(regularAccess));

        assertThrows(SecurityException.class, () -> chatService.approveJoinRequest(roomId, adminId, targetUserId));
        verify(roomAccessRepository, never()).save(any(RoomAccess.class));
    }

    @Test
    void approveJoinRequest_WhenApproverIsAdminAndTargetHasNoAccess_GrantsAccessAndSavesSystemMessage() {
        Long roomId = 1L;
        String adminId = "real-admin-id";
        String targetUserId = "target-user-id";

        RoomAccess adminAccess = new RoomAccess(roomId, adminId, true);
        User adminUser = new User();

        User targetUser = new User();
        targetUser.setDisplayName("John Doe");

        Chat pendingRequest = new Chat();

        when(roomAccessRepository.findByRoomIdAndUserId(roomId, adminId)).thenReturn(Optional.of(adminAccess));
        when(chatRepository.findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(roomId, targetUserId, MessageType.REQUEST))
                .thenReturn(Optional.of(pendingRequest));
        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, targetUserId)).thenReturn(false);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser)); // <-- This was the missing line!

        chatService.approveJoinRequest(roomId, adminId, targetUserId);

        verify(roomAccessRepository).save(any(RoomAccess.class));
        verify(chatRepository).save(any(Chat.class));
    }

    @Test
    void rejectJoinRequest_WhenApproverIsAdmin_DeletesJoinRequest() {
        Long roomId = 1L;
        String adminId = "real-admin-id";
        String targetUserId = "target-user-id";
        RoomAccess adminAccess = new RoomAccess(roomId, adminId, true);
        Chat requestChat = new Chat();

        when(roomAccessRepository.findByRoomIdAndUserId(roomId, adminId)).thenReturn(Optional.of(adminAccess));
        when(chatRepository.findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(roomId, targetUserId, MessageType.REQUEST))
                .thenReturn(Optional.of(requestChat));

        chatService.rejectJoinRequest(roomId, adminId, targetUserId);

        verify(chatRepository).delete(requestChat);
    }

    @Test
    void clearRoomChat_WhenInvoked_WipesChatAndAccessRecords() {
        Long roomId = 1L;

        chatService.clearRoomChat(roomId);

        verify(chatRepository).deleteByRoomId(roomId);
        verify(roomAccessRepository).deleteByRoomId(roomId);
    }
}