package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.FriendRequest;
import ge.freeroom.freeroom.entities.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiverIdAndStatus(String receiverId, FriendRequestStatus status);

    List<FriendRequest> findBySenderIdAndStatus(String senderId, FriendRequestStatus status);

    @Query("""
        SELECT r FROM FriendRequest r
        WHERE r.status = 'PENDING'
        AND (
            (r.sender.id = :a AND r.receiver.id = :b)
            OR
            (r.sender.id = :b AND r.receiver.id = :a)
        )
        """)
    Optional<FriendRequest> findPendingBetweenUsers(@Param("a") String a, @Param("b") String b);

    @Query("""
        SELECT r FROM FriendRequest r
        WHERE r.status = 'PENDING'
        AND (r.sender.id = :userId OR r.receiver.id = :userId)
        """)
    List<FriendRequest> findPendingRequestsInvolvingUser(@Param("userId") String userId);
}
