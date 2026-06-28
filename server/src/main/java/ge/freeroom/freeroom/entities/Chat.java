package ge.freeroom.freeroom.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author", referencedColumnName = "id", nullable = false)
    private User authorUser;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "sending_time", nullable = false)
    private LocalDateTime sendingTime;

    @Column(name = "message_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    public Chat() {}

    public Chat(Long roomId, User authorUser, String message, MessageType messageType) {
        this.roomId = roomId;
        this.authorUser = authorUser;
        this.message = message;
        this.sendingTime = LocalDateTime.now();
        this.messageType = messageType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public User getAuthorUser() { return authorUser; }
    public void setAuthorUser(User authorUser) { this.authorUser = authorUser; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSendingTime() { return sendingTime; }
    public void setSendingTime(LocalDateTime sendingTime) { this.sendingTime = sendingTime; }
    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
}
