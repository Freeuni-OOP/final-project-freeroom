package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    Optional<Lecture> findByEventExternalId(String eventExternalId);
    List<Lecture> findByRoomIdOrderByStartAtAsc(Long roomId);
    List<Lecture> findByStartAtBetween(LocalDateTime start, LocalDateTime end);
    List<Lecture> findByRoomFloorNumberOrderByStartAtAsc(int floorNumber);
    List<Lecture> findByRoomRoomNumberOrderByStartAtAsc(Integer roomNumber);
}
