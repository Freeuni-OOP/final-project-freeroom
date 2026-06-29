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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private TimeService timeService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void getMessages_WhenUserIsNonMember_ThrowsSecurityException() {
        Long roomId = 1L;
        Long beforeId = 100L;
        String userId = "unauthorized-user";
        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(false);

        assertThrows(SecurityException.class, () -> chatService.getMessages(roomId, beforeId, userId));

        verify(chatRepository, never()).findLatestMessages(any(), any());
        verify(chatRepository, never()).findOlderMessages(any(), any(), any());
    }

    @Test
    void getMessages_WhenBeforeIdIsNull_ReturnsLatestMessages() {
        Long roomId = 1L;
        Long beforeId = null;
        String userId = "authorized-user";

        ChatMessageDto msg = new ChatMessageDto(10L, "user", "Nick", "t@edu.ge", "Latest", MessageType.TEXT, LocalDateTime.now());
        List<ChatMessageDto> repoReturnedMessages = List.of(msg);

        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatRepository.findLatestMessages(roomId, PageRequest.of(0, 20))).thenReturn(repoReturnedMessages);

        List<ChatMessageDto> actualMessages = chatService.getMessages(roomId, beforeId, userId);

        assertEquals(repoReturnedMessages, actualMessages);
        verify(chatRepository, times(1)).findLatestMessages(roomId, PageRequest.of(0, 20));
        verify(chatRepository, never()).findOlderMessages(any(), any(), any());
    }

    @Test
    void getMessages_WhenUserIsMemberAndBeforeIdProvided_ReturnsOlderMessagesInChronologicalOrder() {
        Long roomId = 1L;
        Long beforeId = 50L;
        String userId = "authorized-user";

        ChatMessageDto msg1 = new ChatMessageDto(10L, "user", "Nick", "t@edu.ge", "First", MessageType.TEXT, LocalDateTime.now());
        ChatMessageDto msg2 = new ChatMessageDto(11L, "user", "Nick", "t@edu.ge", "Second", MessageType.TEXT, LocalDateTime.now());

        List<ChatMessageDto> repoReturnedMessages = List.of(msg2, msg1); // DESC ბაზიდან
        List<ChatMessageDto> expectedChronologicalMessages = List.of(msg1, msg2); // ASC სერვისიდან

        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatRepository.findOlderMessages(roomId, beforeId, PageRequest.of(0, 20))).thenReturn(repoReturnedMessages);

        List<ChatMessageDto> actualMessages = chatService.getMessages(roomId, beforeId, userId);

        assertEquals(expectedChronologicalMessages, actualMessages);
        verify(chatRepository, times(1)).findOlderMessages(roomId, beforeId, PageRequest.of(0, 20));
        verify(chatRepository, never()).findLatestMessages(any(), any());
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

        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(roomId, userId, "Valid room, missing user profile"));
        verify(chatRepository, never()).save(any());
    }

    @Test
    void sendJoinRequest_WhenRateLimitExceeded_ThrowsIllegalStateException() {
        Long roomId = 1L;
        String userId = "spammer-id";
        Chat recentRequest = new Chat(roomId, new User(), "First request", MessageType.REQUEST);
        recentRequest.setSendingTime(LocalDateTime.now().minusSeconds(30));

        when(chatRepository.findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(roomId, userId, MessageType.REQUEST))
                .thenReturn(Optional.of(recentRequest));
        when(timeService.now()).thenReturn(LocalDateTime.now());

        assertThrows(IllegalStateException.class, () -> chatService.sendJoinRequest(roomId, userId));
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void sendJoinRequest_WhenRequestIsOutsideRateLimitWindow_SavesRequest() {
        Long roomId = 1L;
        String userId = "valid-requester-id";
        User user = new User();
        Chat oldRequest = new Chat(roomId, user, "Old request", MessageType.REQUEST);
        oldRequest.setSendingTime(LocalDateTime.now().minusMinutes(2));

        when(chatRepository.findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(roomId, userId, MessageType.REQUEST))
                .thenReturn(Optional.of(oldRequest));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(timeService.now()).thenReturn(LocalDateTime.now());

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
        verify(chatRepository, never()).save(any(Chat.class));
    }

    @Test
    void approveJoinRequest_WhenApproverIsAdminAndTargetHasNoAccess_GrantsAccessAndSavesSystemMessage() {
        Long roomId = 1L;
        String adminId = "real-admin-id";
        String targetUserId = "target-user-id";
        RoomAccess adminAccess = new RoomAccess(roomId, adminId, true);
        User adminUser = new User();

        when(roomAccessRepository.findByRoomIdAndUserId(roomId, adminId)).thenReturn(Optional.of(adminAccess));
        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, targetUserId)).thenReturn(false);
        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(chatRepository.findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(roomId, targetUserId, MessageType.REQUEST))
                .thenReturn(Optional.empty());

        chatService.approveJoinRequest(roomId, adminId, targetUserId);

        verify(roomAccessRepository, times(1)).save(any(RoomAccess.class));
        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    void rejectJoinRequest_WhenApproverIsNotAdmin_ThrowsSecurityException() {
        Long roomId = 1L;
        String adminId = "fake-admin-id";
        String targetUserId = "target-user-id";
        RoomAccess regularAccess = new RoomAccess(roomId, adminId, false);

        when(roomAccessRepository.findByRoomIdAndUserId(roomId, adminId)).thenReturn(Optional.of(regularAccess));

        assertThrows(SecurityException.class, () -> chatService.rejectJoinRequest(roomId, adminId, targetUserId));
        verify(chatRepository, never()).delete(any(Chat.class));
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

        verify(chatRepository, times(1)).delete(requestChat);
    }

    @Test
    void clearRoomChat_WhenInvoked_WipesChatAndAccessRecords() {
        Long roomId = 1L;

        chatService.clearRoomChat(roomId);

        verify(chatRepository, times(1)).deleteByRoomId(roomId);
        verify(roomAccessRepository, times(1)).deleteByRoomId(roomId);
    }
}