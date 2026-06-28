package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Lecture;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    
    @EntityGraph(attributePaths = {"room", "subject", "room.floor"})
    List<Lecture> findAll();

    Optional<Lecture> findByEventExternalId(String eventExternalId);

    @EntityGraph(attributePaths = {"room", "subject", "room.floor"})
    List<Lecture> findByRoomIdOrderByStartAtAsc(Long roomId);

    List<Lecture> findByStartAtBetween(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"room", "subject", "room.floor"})
    List<Lecture> findByRoomFloorNumberOrderByStartAtAsc(int floorNumber);

    @EntityGraph(attributePaths = {"room", "subject", "room.floor"})
    List<Lecture> findByRoomRoomNumberOrderByStartAtAsc(Integer roomNumber);

    @EntityGraph(attributePaths = {"room", "subject", "room.floor"})
    @Query("select l from Lecture l where l.room.id in :roomIds and l.startAt <= :now and l.endAt >= :now")
    List<Lecture> findActiveLecturesByRoomIds(@Param("roomIds") List<Long> roomIds, @Param("now") LocalDateTime now);

    @EntityGraph(attributePaths = {"room", "subject", "room.floor"})
    @Query("select l from Lecture l where lower(l.subject.title) like lower(:searchTerm) order by l.startAt asc, l.room.roomNumber ASC")
    List<Lecture> searchLecturesChronologically(@Param("searchTerm") String searchTerm);
  
    @EntityGraph(attributePaths = {"room", "subject", "room.floor"})
    @Query("select l from Lecture l where l.room.id in :roomIds and l.startAt > :now and l.startAt < :endOfDay order by l.startAt asc")
    List<Lecture> findUpcomingLecturesTodayByRoomIds(
                            @Param("roomIds") List<Long> roomIds,
                            @Param("now") LocalDateTime now,
                            @Param("endOfDay") LocalDateTime endOfDay);
}
