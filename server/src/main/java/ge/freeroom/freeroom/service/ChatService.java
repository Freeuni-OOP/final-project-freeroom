package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.ChatMessageDto;
import ge.freeroom.freeroom.entities.Chat;
import ge.freeroom.freeroom.entities.MessageType;
import ge.freeroom.freeroom.entities.RoomAccess;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.ChatRepository;
import ge.freeroom.freeroom.repositories.RoomAccessRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private RoomAccessRepository roomAccessRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ChatMessageDto> getMessages(Long roomId, String userId) {
        if (!roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new SecurityException("Unauthorized access to room chat.");
        }
        return chatRepository.findMessagesByRoomId(roomId);
    }

    public void sendMessage(Long roomId, String authorId, String message) {
        if (!roomAccessRepository.existsByRoomIdAndUserId(roomId, authorId)) {
            throw new SecurityException("Unauthorized to send messages in this room.");
        }
        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Chat newChat = new Chat(roomId, user, message, MessageType.TEXT);
        chatRepository.save(newChat);
    }

    public void sendJoinRequest(Long roomId, String requesterId) {
        Optional<Chat> lastRequest = chatRepository.findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(roomId, requesterId, MessageType.REQUEST);
        if (lastRequest.isPresent() && lastRequest.get().getSendingTime().isAfter(LocalDateTime.now().minusMinutes(1))) {
            throw new IllegalStateException("Rate limit exceeded. You can only request once per minute.");
        }
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Chat requestChat = new Chat(roomId, user, "Requesting access to the room.", MessageType.REQUEST);
        chatRepository.save(requestChat);
    }

    @Transactional
    public void approveJoinRequest(Long roomId, String adminId, String targetUserId) {
        RoomAccess adminAccess = roomAccessRepository.findByRoomIdAndUserId(roomId, adminId)
                .orElseThrow(() -> new SecurityException("Unauthorized."));
        if (!adminAccess.isAdmin()) {
            throw new SecurityException("Only admins can approve requests.");
        }
        if (!roomAccessRepository.existsByRoomIdAndUserId(roomId, targetUserId)) {
            RoomAccess newAccess = new RoomAccess(roomId, targetUserId, false);
            roomAccessRepository.save(newAccess);
        }
        User adminUser = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Chat approvalChat = new Chat(roomId, adminUser, "Approved access to join.", MessageType.APPROVAL);
        chatRepository.save(approvalChat);
    }

    @Transactional
    public void initializeRoomBooker(Long roomId, String bookerId) {
        if (!roomAccessRepository.existsByRoomIdAndUserId(roomId, bookerId)) {
            RoomAccess bookerAccess = new RoomAccess(roomId, bookerId, true);
            roomAccessRepository.save(bookerAccess);
        }
    }

    @Transactional
    public void clearRoomChat(Long roomId) {
        chatRepository.deleteByRoomId(roomId);
        roomAccessRepository.deleteByRoomId(roomId);
    }
}
