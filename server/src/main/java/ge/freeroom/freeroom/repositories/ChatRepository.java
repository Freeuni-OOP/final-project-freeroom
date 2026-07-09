package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.dto.ChatMessageDto;
import ge.freeroom.freeroom.entities.Chat;
import ge.freeroom.freeroom.entities.MessageType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT new ge.freeroom.freeroom.dto.ChatMessageDto(c.id, u.id, u.displayName, u.email, u.photoUrl, c.message, c.messageType, c.sendingTime) " +
            "FROM Chat c JOIN c.authorUser u " +
            "WHERE c.roomId = :roomId " +
            "ORDER BY c.id DESC")
    List<ChatMessageDto> findLatestMessages(@Param("roomId") Long roomId, Pageable pageable);

    @Query("SELECT new ge.freeroom.freeroom.dto.ChatMessageDto(c.id, u.id, u.displayName, u.email, u.photoUrl, c.message, c.messageType, c.sendingTime) " +
            "FROM Chat c JOIN c.authorUser u " +
            "WHERE c.roomId = :roomId AND c.id < :beforeId " +
            "ORDER BY c.id DESC")
    List<ChatMessageDto> findOlderMessages(@Param("roomId") Long roomId, @Param("beforeId") Long beforeId, Pageable pageable);

    Optional<Chat> findFirstByRoomIdAndAuthorUser_IdAndMessageTypeOrderBySendingTimeDesc(Long roomId, String authorId, MessageType messageType);

    void deleteByRoomId(Long roomId);
}