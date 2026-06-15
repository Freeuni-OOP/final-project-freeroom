package ge.freeroom.freeroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReserveRoomRequest {
    private Long roomDbId;
    private Integer roomNumber;
    private Long durationMinutes;
}
