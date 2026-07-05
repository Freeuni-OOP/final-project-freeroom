package ge.freeroom.freeroom.dto;

import ge.freeroom.freeroom.entities.ReportReason;
import ge.freeroom.freeroom.entities.ReportStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class UserReportResponseDto {
    private Long id;
    private String reportedUserId;
    private String reportedUserDisplayName;
    private String reporterUserId;
    private String reporterUserDisplayName;
    private ReportReason reason;
    private String details;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
