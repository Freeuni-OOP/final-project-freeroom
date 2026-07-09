package ge.freeroom.freeroom.dto;

import ge.freeroom.freeroom.entities.NotificationType;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        NotificationType type,
        String actorId,
        String actorDisplayName,
        String actorPhotoUrl,
        Long referenceId,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {}
