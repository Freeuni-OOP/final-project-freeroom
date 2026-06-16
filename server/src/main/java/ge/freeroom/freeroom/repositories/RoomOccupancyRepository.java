package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.RoomOccupancy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomOccupancyRepository extends JpaRepository<RoomOccupancy, Long> {
    Optional<RoomOccupancy> findFirstByRoomIdAndEndAtIsNull(Long roomId);
    List<RoomOccupancy> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    List<RoomOccupancy> findByRoomIdInAndEndAtIsNull(List<Long> roomIds);
}