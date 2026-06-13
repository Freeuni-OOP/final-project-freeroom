package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
    List<WaitlistEntry> findByRoomIdOrderByCreatedAtAsc(Long roomId);
    Optional<WaitlistEntry> findByRoomIdAndUserIdAndCancelledFalse(Long roomId, String userId);
}