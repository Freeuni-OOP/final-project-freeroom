package ge.freeroom.freeroom.dto;

import ge.freeroom.freeroom.entities.FriendRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FriendRequestDto {
    private Long requestId;
    private String senderId;
    private String senderDisplayName;
    private String senderPhotoUrl;
    private String receiverId;
    private String receiverDisplayName;
    private String receiverPhotoUrl;
    private FriendRequestStatus status;
    private LocalDateTime createdAt;
}
