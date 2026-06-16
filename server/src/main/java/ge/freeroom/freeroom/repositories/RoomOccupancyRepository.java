package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.RoomOccupancy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface RoomOccupancyRepository extends JpaRepository<RoomOccupancy, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RoomOccupancy> findFirstByRoomIdAndEndAtIsNull(Long roomId);
    List<RoomOccupancy> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    List<RoomOccupancy> findByRoomIdInAndEndAtIsNull(List<Long> roomIds);
}