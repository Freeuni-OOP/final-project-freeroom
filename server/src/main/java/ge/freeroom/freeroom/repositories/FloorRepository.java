package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FloorRepository extends JpaRepository<Floor, Long> {
    Optional<Floor> findByNumber(int number);
}
