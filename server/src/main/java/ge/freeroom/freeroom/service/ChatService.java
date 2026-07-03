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
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private RoomAccessRepository roomAccessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public List<ChatMessageDto> getMessages(Long roomId, Long beforeId, String userId) {
        if (!roomAccessRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new SecurityException("Unauthorized access to room chat.");
        }

        List<ChatMessageDto> messages;
        PageRequest pageRequest = PageRequest.of(0, 20);

        if (beforeId == null) {
            messages = chatRepository.findLatestMessages(roomId, pageRequest);
        } else {
            messages = chatRepository.findOlderMessages(roomId, beforeId, pageRequest);
        }

        List<ChatMessageDto> chronologicalMessages = new ArrayList<>(messages);
        Collections.reverse(chronologicalMessages);

        return chronologicalMessages;
    }

    public void sendMessage(Long roomId, String authorId, String message) {
        if (!roomAccessRepository.existsByRoomIdAndUserId(roomId, authorId)) {
            throw new SecurityException("Unauthorized to send messages in this room.");
        }
        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Chat newChat = new Chat(roomId, user, message, MessageType.TEXT);
        chatRepository.save(newChat);

        broadcastMessage(roomId, newChat, user);
    }

    public void sendJoinRequest(Long roomId, String requesterId) {
        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Chat requestChat = new Chat(roomId, user, "Requesting access to the room.", MessageType.REQUEST);
        chatRepository.save(requestChat);

        broadcastMessage(roomId, requestChat, user);
    }

    @Transactional
    public void approveJoinRequest(Long roomId, String adminId, String targetUserId) {
        RoomAccess adminAccess = roomAccessRepository.findByRoomIdAndUserId(roomId, adminId)
                .orElseThrow(() -> new SecurityException("Unauthorized."));
        if (!adminAccess.isAdmin()) {
            throw new SecurityException("Only admins can approve requests.");
        }

        chatRepository.findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(roomId, targetUserId, MessageType.REQUEST)
                .orElseThrow(() -> new IllegalStateException("No pending join request from this user"));

        if (!roomAccessRepository.existsByRoomIdAndUserId(roomId, targetUserId)) {
            RoomAccess newAccess = new RoomAccess(roomId, targetUserId, false);
            roomAccessRepository.save(newAccess);
        }
        User adminUser = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Chat approvalChat = new Chat(roomId, adminUser, "Approved access to join.", MessageType.APPROVAL);
        chatRepository.save(approvalChat);

        chatRepository.findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(roomId, targetUserId, MessageType.REQUEST)
                .ifPresent(chat -> chatRepository.delete(chat));

        broadcastMessage(roomId, approvalChat, adminUser);
        broadcastReload(roomId);
    }

    @Transactional
    public void rejectJoinRequest(Long roomId, String adminId, String targetUserId) {
        RoomAccess adminAccess = roomAccessRepository.findByRoomIdAndUserId(roomId, adminId)
                .orElseThrow(() -> new SecurityException("Unauthorized."));
        if (!adminAccess.isAdmin()) {
            throw new SecurityException("Only admins can reject requests.");
        }
        chatRepository.findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(roomId, targetUserId, MessageType.REQUEST)
                .ifPresent(chat -> chatRepository.delete(chat));

        broadcastReload(roomId);
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
        broadcastReload(roomId);
    }


    private void broadcastMessage(Long roomId, Chat chat, User user) {
        ChatMessageDto dto = new ChatMessageDto(
                chat.getId(),
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                chat.getMessage(),
                chat.getMessageType(),
                chat.getSendingTime()
        );
        messagingTemplate.convertAndSend("/topic/room/" + roomId, dto);
    }

    private void broadcastReload(Long roomId) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/reload", "RELOAD");
    }
}