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
public class CancelOccupancyResponseDto {
    private Long occupancyId;
    private Long roomId;
    private Integer roomNumber;
    private LocalDateTime cancelledAt;
    private String message;
}
