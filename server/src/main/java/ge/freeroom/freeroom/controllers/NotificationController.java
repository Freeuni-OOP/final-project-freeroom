package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.NotificationDto;
import ge.freeroom.freeroom.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Page<NotificationDto> getNotifications(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return notificationService.getNotifications(principal.getName(), page, size);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Principal principal) {
        return Map.of("count", notificationService.getUnreadCount(principal.getName()));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Principal principal) {
        notificationService.markAllRead(principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markOneRead(@PathVariable Long id, Principal principal) {
        notificationService.markOneRead(principal.getName(), id);
        return ResponseEntity.ok().build();
    }
}
