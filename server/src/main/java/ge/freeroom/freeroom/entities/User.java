package ge.freeroom.freeroom.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "app_user")
public class User {
    @Id
    private String id; // Firebase UID

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "reputation_points")
    private int reputationPoints = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) default 'NONE'")
    private NotificationPreference notificationPreference = NotificationPreference.NONE;

    @Column(nullable = true)
    private Long telegramChatId;

    @Column(nullable = true)
    private String telegramLinkToken;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<RoomOccupancy> occupancies;

    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<WaitlistEntry> waitlistEntries;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}