package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(Integer roomNumber);
    List<Room> findByFloorId(Long floorId);
}
