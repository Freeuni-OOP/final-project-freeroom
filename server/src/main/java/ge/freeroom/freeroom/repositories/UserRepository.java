package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTelegramLinkToken(String telegramLinkToken);
}
