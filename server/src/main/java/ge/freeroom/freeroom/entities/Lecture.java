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
@Table(name = "lecture",
        indexes = {
                @Index(columnList = "event_external_id"),
                @Index(columnList = "room_id, start_at")
        })
public class Lecture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_external_id", unique = true, nullable = false)
    private String eventExternalId; // Google Calendar event id

    private String title; // e.g. "OOP"
    private String description; // lecture, seminar, etc
    private String organizer; // lecturer name/email

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    private boolean recurring;
    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;
}