package ge.freeroom.freeroom.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReserveRoomRequestDto {
    private Long roomDbId;
    private Integer roomNumber;
    @Min(1)
    @Max(480)
    private Long durationMinutes;
    @jakarta.validation.constraints.Size(max = 40)
    private String publicNote;
}
