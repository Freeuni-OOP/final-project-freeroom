package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
        AND u.id != :excludeId
        """)
    List<User> searchByDisplayName(
            @Param("query") String query,
            @Param("excludeId") String excludeId,
            Pageable pageable
    );
}
