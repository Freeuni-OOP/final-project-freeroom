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
public class RoomOccupancySummaryDto {
    private LocalDateTime startAt;
    private LocalDateTime expectedEndAt;
    private Boolean isMyOccupancy;
    private Boolean isFriendOccupancy;

    // (NOT SHOWING PUBLICLY FOR PRIVACY) :
    private String reserverDisplayName; // for Friend invite or something in future
    private String reserverPhotoUrl;
    private String reserverId;
    
    // PUBLIC FIELD:
    private String publicNote;
}

