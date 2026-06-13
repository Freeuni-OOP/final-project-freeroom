package ge.freeroom.freeroom.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "room",
        indexes = {
                @Index(columnList = "floor_id"),
                @Index(columnList = "room_number")
        })
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true)
    private Integer roomNumber; // 1xx/2xx/3xx/4xx

    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id")
    private Floor floor;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "room")
    private List<Lecture> lectures;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "room")
    private List<RoomOccupancy> occupancies;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @OneToMany(mappedBy = "room")
    private List<WaitlistEntry> waitlistEntries;
}