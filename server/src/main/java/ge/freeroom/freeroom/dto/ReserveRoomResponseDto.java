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
public class ReserveRoomResponseDto {
    private Long id;
    private Long roomId;
    private Integer roomNumber;
    private LocalDateTime startTime;
    private LocalDateTime expectedEndTime;
}
