package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.RoomAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoomAccessRepository extends JpaRepository<RoomAccess, Long> {
    boolean existsByRoomIdAndUserId(Long roomId, String userId);
    Optional<RoomAccess> findByRoomIdAndUserId(Long roomId, String userId);
    void deleteByRoomId(Long roomId);
    Optional<RoomAccess> findFirstByRoomIdAndIsAdminTrue(Long roomId);
    List<RoomAccess> findByRoomId(Long roomId);
}