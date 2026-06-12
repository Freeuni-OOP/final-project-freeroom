package ge.freeroom.freeroom.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "app_user") // "user" is reserved in some DBs
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

    @OneToMany(mappedBy = "user")
    private List<RoomOccupancy> occupancies;

    @OneToMany(mappedBy = "user")
    private List<WaitlistEntry> waitlistEntries;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
