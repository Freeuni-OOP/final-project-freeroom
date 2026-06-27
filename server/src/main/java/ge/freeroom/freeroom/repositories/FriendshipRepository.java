package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("""
        SELECT CASE
            WHEN f.user1.id = :userId THEN f.user2.id
            ELSE f.user1.id
        END
        FROM Friendship f
        WHERE f.user1.id = :userId OR f.user2.id = :userId
        """)
    List<String> findFriendIdsByUserId(@Param("userId") String userId);

    @Query("""
        SELECT COUNT(f) > 0 FROM Friendship f
        WHERE (f.user1.id = :a AND f.user2.id = :b)
           OR (f.user1.id = :b AND f.user2.id = :a)
        """)
    boolean existsByUsers(@Param("a") String a, @Param("b") String b);

    @Query("""
        SELECT f FROM Friendship f
        WHERE (f.user1.id = :a AND f.user2.id = :b)
           OR (f.user1.id = :b AND f.user2.id = :a)
        """)
    Optional<Friendship> findByUsers(@Param("a") String a, @Param("b") String b);
}
