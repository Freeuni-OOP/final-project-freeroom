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
public class FriendDto {
    private String id;
    private String displayName;
    private String photoUrl;
    private boolean hasActiveOccupancy;
    private OccupancyInfo currentOccupancy; // null when hasActiveOccupancy is false

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OccupancyInfo {
        private Integer roomNumber;
        private int floorNumber;
        private LocalDateTime startAt;
        private LocalDateTime expectedEndAt;
    }
}
