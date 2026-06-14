package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    Optional<Lecture> findByEventExternalId(String eventExternalId);
    List<Lecture> findByRoomIdOrderByStartAtAsc(Long roomId);
    List<Lecture> findByStartAtBetween(LocalDateTime start, LocalDateTime end);
    List<Lecture> findByRoomFloorNumberOrderByStartAtAsc(int floorNumber);
    List<Lecture> findByRoomRoomNumberOrderByStartAtAsc(Integer roomNumber);

    @Query("select l from Lecture l where l.room.id in :roomIds and l.startAt <= :now and l.endAt >= :now")
    List<Lecture> findActiveLecturesByRoomIds(@Param("roomIds") List<Long> roomIds, @Param("now") LocalDateTime now);
}
