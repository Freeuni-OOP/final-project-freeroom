package ge.freeroom.freeroom.websocket.dto;

import ge.freeroom.freeroom.websocket.events.RoomEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RoomEventDto {
    private RoomEventType type;
    private Long roomId;
    private Integer roomNumber;
    private Integer floorNumber;
    private LocalDateTime timestamp;
}