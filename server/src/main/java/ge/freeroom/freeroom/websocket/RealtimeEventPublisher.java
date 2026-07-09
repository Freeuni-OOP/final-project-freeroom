package ge.freeroom.freeroom.websocket;

import ge.freeroom.freeroom.service.TimeService;
import ge.freeroom.freeroom.websocket.dto.FriendEventDto;
import ge.freeroom.freeroom.websocket.dto.ProfileEventDto;
import ge.freeroom.freeroom.websocket.events.FriendEventType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class RealtimeEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;
    private final TimeService timeService;

    public RealtimeEventPublisher(SimpMessagingTemplate messagingTemplate, TimeService timeService) {
        this.messagingTemplate = messagingTemplate;
        this.timeService = timeService;
    }

    public void publishFriendEvent(String targetUserId, Object payload) {
        publishAfterCommit("/topic/users/" + targetUserId + "/friends", payload);
    }

    public void publishRoomEvent(Object payload) {
        publishAfterCommit("/topic/rooms", payload);
    }

    public void publishProfileEvent(String targetUserId, Object payload) {
        publishAfterCommit("/topic/users/" + targetUserId + "/profile", payload);
    }

    public void publishNotificationEvent(String targetUserId, Object payload) {
        publishAfterCommit("/topic/users/" + targetUserId + "/notifications", payload);
    }

    public void publishOccupancyRipple(String userId, java.util.List<String> friendIds) {
        FriendEventDto friendPayload = new FriendEventDto(
                FriendEventType.OCCUPANCY_CHANGED, null, userId, null, null, timeService.now()
        );
        for (String friendId : friendIds) {
            publishAfterCommit("/topic/users/" + friendId + "/friends", friendPayload);
        }
        publishAfterCommit("/topic/users/" + userId + "/profile", new ProfileEventDto(userId, timeService.now()));
    }

    private void publishAfterCommit(String destination, Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    messagingTemplate.convertAndSend(destination, payload);
                }
            });
        } else {
            messagingTemplate.convertAndSend(destination, payload);
        }
    }
}