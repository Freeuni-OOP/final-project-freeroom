package ge.freeroom.freeroom.dto;

import jakarta.validation.constraints.NotNull;

public record JoinRoomRequestDto(
        @NotNull
        Long roomId
) {}