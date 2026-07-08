package ge.freeroom.freeroom.dto;

import jakarta.validation.constraints.NotNull;

public record KickUserRequestDto(
        @NotNull Long roomId,
        @NotNull String targetUserId
) {}