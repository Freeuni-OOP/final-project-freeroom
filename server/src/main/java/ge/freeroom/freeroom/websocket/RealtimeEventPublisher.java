package ge.freeroom.freeroom.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class RealtimeEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishFriendEvent(String targetUserId, Object payload) {
        messagingTemplate.convertAndSend("/topic/users/" + targetUserId + "/friends", payload);
    }
}