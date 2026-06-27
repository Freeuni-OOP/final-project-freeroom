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

    @InjectMocks
    private ChatService chatService;

    @Test
    void getMessages_WhenUserIsNonMember_ThrowsSecurityException() {
        Long roomId = 1L;
        String userId = "unauthorized-user";
        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(false);

        assertThrows(SecurityException.class, () -> chatService.getMessages(roomId, userId));
        verify(chatRepository, never()).findMessagesByRoomId(any());
    }

    @Test
    void getMessages_WhenUserIsMember_ReturnsMessages() {
        Long roomId = 1L;
        String userId = "authorized-user";
        List<ChatMessageDto> expectedMessages = List.of(
                new ChatMessageDto("Nick", "test@freeuni.edu.ge", "Hello", MessageType.TEXT, LocalDateTime.now())
        );

        when(roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)).thenReturn(true);
        when(chatRepository.findMessagesByRoomId(roomId)).thenReturn(expectedMessages);

        List<ChatMessageDto> actualMessages = chatService.getMessages(roomId, userId);

        assertEquals(expectedMessages, actualMessages);
        verify(chatRepository, times(1)).findMessagesByRoomId(roomId);
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

        chatService.approveJoinRequest(roomId, adminId, targetUserId);

        verify(roomAccessRepository, times(1)).save(any(RoomAccess.class));
        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    void clearRoomChat_WhenInvoked_WipesChatAndAccessRecords() {
        Long roomId = 1L;

        chatService.clearRoomChat(roomId);

        verify(chatRepository, times(1)).deleteByRoomId(roomId);
        verify(roomAccessRepository, times(1)).deleteByRoomId(roomId);
    }
}
