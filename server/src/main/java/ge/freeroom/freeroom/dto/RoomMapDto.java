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
public class RoomMapDto {
    private Long id;
    private Integer roomNumber;
    private Integer capacity;
    private Integer floorNumber;
    private String status; // "free"/"occupied"
    private LectureSummaryDto currentLecture; // null if free
    private RoomOccupancySummaryDto currentOccupancy; // null if free
    private LectureSummaryDto nextLecture; // null if no upcoming lecture that day
    private LocalDateTime serverNow;
}
