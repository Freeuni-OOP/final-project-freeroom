package ge.freeroom.freeroom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "room_occupancy",
        indexes = {
                @Index(columnList = "room_id, end_at"),
                @Index(columnList = "user_id")
        })
public class RoomOccupancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    @Column(name = "expected_end_at", nullable = false)
    private LocalDateTime expectedEndAt;
    @Column(name = "end_at")
    private LocalDateTime endAt; // actual checkout, use endAt == null to check active status

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}