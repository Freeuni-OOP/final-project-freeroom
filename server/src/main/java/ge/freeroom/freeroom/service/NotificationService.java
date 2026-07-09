package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.NotificationDto;
import ge.freeroom.freeroom.entities.Notification;
import ge.freeroom.freeroom.entities.NotificationType;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.NotificationRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.websocket.RealtimeEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            RealtimeEventPublisher realtimeEventPublisher) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.realtimeEventPublisher = realtimeEventPublisher;
    }

    @Transactional
    public void createAndPublish(String recipientId, NotificationType type, User actor, Long referenceId, String message) {
        User recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null) return;

        Notification notification = new Notification(recipient, actor, type, referenceId, message);
        notificationRepository.save(notification);

        NotificationDto dto = toDto(notification);
        realtimeEventPublisher.publishNotificationEvent(recipientId, dto);
    }

    public void publishToast(String recipientId, NotificationType type, User actor, Long referenceId, String message) {
        NotificationDto dto = new NotificationDto(
                null, type,
                actor != null ? actor.getId() : null,
                actor != null ? actor.getDisplayName() : null,
                actor != null ? actor.getPhotoUrl() : null,
                referenceId, message, false, java.time.LocalDateTime.now()
        );
        realtimeEventPublisher.publishNotificationEvent(recipientId, dto);
    }

    public List<NotificationDto> getNotifications(String userId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 30))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllRead(String userId) {
        notificationRepository.markAllReadByRecipientId(userId);
    }

    @Transactional
    public void markOneRead(String userId, Long notifId) {
        notificationRepository.findById(notifId).ifPresent(n -> {
            if (n.getRecipient().getId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    private NotificationDto toDto(Notification n) {
        User actor = n.getActor();
        return new NotificationDto(
                n.getId(),
                n.getType(),
                actor != null ? actor.getId() : null,
                actor != null ? actor.getDisplayName() : null,
                actor != null ? actor.getPhotoUrl() : null,
                n.getReferenceId(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
