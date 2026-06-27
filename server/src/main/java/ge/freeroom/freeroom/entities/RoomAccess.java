package ge.freeroom.freeroom.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "room_access")
public class RoomAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    public RoomAccess() {}

    public RoomAccess(Long roomId, String userId, boolean isAdmin) {
        this.roomId = roomId;
        this.userId = userId;
        this.isAdmin = isAdmin;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}