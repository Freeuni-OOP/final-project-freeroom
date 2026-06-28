package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findFirstByTitleAndTypeAndGroupNumberAndLecturer(String title, String type, String groupNumber, String lecturer);
}
