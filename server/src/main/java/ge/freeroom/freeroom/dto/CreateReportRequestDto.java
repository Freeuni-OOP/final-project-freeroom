package ge.freeroom.freeroom.dto;

import ge.freeroom.freeroom.entities.ReportReason;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReportRequestDto {
    private ReportReason reason;
    private String details;
}
