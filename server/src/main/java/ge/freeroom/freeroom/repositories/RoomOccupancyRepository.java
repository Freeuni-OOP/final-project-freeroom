package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.RoomOccupancy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RoomOccupancyRepository extends JpaRepository<RoomOccupancy, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM RoomOccupancy o WHERE o.room.id = :roomId AND o.endAt IS NULL AND o.expectedEndAt > :now")
    Optional<RoomOccupancy> findFirstByRoomIdAndEndAtIsNull(@Param("roomId") Long roomId, @Param("now") LocalDateTime now);
    List<RoomOccupancy> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    List<RoomOccupancy> findByRoomIdInAndEndAtIsNull(List<Long> roomIds);

    // added this query to replace the last one(will leave that just in case)
    // this one also checks if expected time is not expired, so we don't return occupancies that are already expired
    @Query("SELECT o FROM RoomOccupancy o WHERE o.room.id IN :roomIds AND o.endAt IS NULL AND o.expectedEndAt > :now")
    List<RoomOccupancy> findActiveNonExpiredByRoomIds(@Param("roomIds") List<Long> roomIds, @Param("now") LocalDateTime now);
}