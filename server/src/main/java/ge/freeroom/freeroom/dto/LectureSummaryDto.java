package ge.freeroom.freeroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LectureSummaryDto {
    private String title;
    private String type;
    private String groupNumber;
    private String organizer;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
