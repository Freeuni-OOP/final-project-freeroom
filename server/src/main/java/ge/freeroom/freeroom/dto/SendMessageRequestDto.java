package ge.freeroom.freeroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequestDto(
        @NotNull
        Long roomId,

        @NotBlank
        @Size(max = 2000)
        String message
) {}