package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByRoomIdOrderBySendingTimeAsc(Long roomId);
    Optional<Chat> findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(Long roomId, String authorId, String messageType);
    void deleteByRoomId(Long roomId);
}