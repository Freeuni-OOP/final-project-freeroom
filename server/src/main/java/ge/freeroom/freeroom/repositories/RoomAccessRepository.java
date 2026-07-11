package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.dto.RoomMemberDto;
import ge.freeroom.freeroom.entities.RoomAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomAccessRepository extends JpaRepository<RoomAccess, Long> {
    boolean existsByRoomIdAndUserId(Long roomId, String userId);
    Optional<RoomAccess> findByRoomIdAndUserId(Long roomId, String userId);
    void deleteByRoomId(Long roomId);
    Optional<RoomAccess> findFirstByRoomIdAndIsAdminTrue(Long roomId);
    List<RoomAccess> findByRoomId(Long roomId);

    @Query("SELECT new ge.freeroom.freeroom.dto.RoomMemberDto(u.id, u.displayName, u.photoUrl, ra.isAdmin) " +
            "FROM RoomAccess ra JOIN User u ON ra.userId = u.id " +
            "WHERE ra.roomId = :roomId")
    List<RoomMemberDto> findMembersByRoomId(@Param("roomId") Long roomId);
}