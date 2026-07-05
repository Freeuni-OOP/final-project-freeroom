package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.ReportStatus;
import ge.freeroom.freeroom.entities.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    boolean existsByReportedUser_IdAndReporterUser_Id(String reportedUserId, String reporterUserId);
}