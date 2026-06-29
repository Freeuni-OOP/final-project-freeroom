package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findFirstByTitleAndTypeAndGroupNumberAndLecturer(String title, String type, String groupNumber, String lecturer);

    @Query("SELECT DISTINCT s FROM Lecture l JOIN l.subject s ORDER BY s.title ASC")
    List<Subject> findAllActiveSubjects();

    @Query("SELECT DISTINCT s FROM Lecture l JOIN l.subject s WHERE lower(s.title) like lower(:searchTerm) or lower(s.lecturer) like lower(:searchTerm) order by s.title asc")
    List<Subject> searchSubjects(@Param("searchTerm") String searchTerm);
}
