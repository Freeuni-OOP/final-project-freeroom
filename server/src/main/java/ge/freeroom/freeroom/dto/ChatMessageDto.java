package ge.freeroom.freeroom.dto;

import ge.freeroom.freeroom.entities.MessageType;

import java.time.LocalDateTime;

public record ChatMessageDto(
        String nickname,
        String email,
        String message,
        MessageType messageType,
        LocalDateTime sendingTime
) {}
