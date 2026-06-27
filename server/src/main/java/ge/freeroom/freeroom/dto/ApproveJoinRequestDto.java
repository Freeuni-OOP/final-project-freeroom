package ge.freeroom.freeroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApproveJoinRequestDto(
        @NotNull
        Long roomId,

        @NotBlank
        String targetUserId
) {}