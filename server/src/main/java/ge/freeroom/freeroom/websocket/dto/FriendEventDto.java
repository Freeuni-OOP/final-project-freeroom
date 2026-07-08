package ge.freeroom.freeroom.websocket.dto;

import ge.freeroom.freeroom.websocket.events.FriendEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FriendEventDto {
    private FriendEventType type;
    private Long requestId;
    private String actorId;
    private String actorDisplayName;
    private String actorPhotoUrl;
    private LocalDateTime timestamp;
}