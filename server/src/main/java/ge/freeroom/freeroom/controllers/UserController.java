package ge.freeroom.freeroom.controllers;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.dto.NotificationPreferenceResponseDto;
import ge.freeroom.freeroom.dto.TelegramLinkResponseDto;
import ge.freeroom.freeroom.dto.UpdateNotificationPreferenceRequest;
import ge.freeroom.freeroom.entities.NotificationPreference;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncUser(HttpServletRequest request, Principal principal) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        String uid = token.getUid();

        if (!userRepository.existsById(uid)) {
            User user = new User();
            user.setId(uid);
            user.setEmail(token.getEmail());
            user.setDisplayName(token.getName());
            user.setPhotoUrl(token.getPicture());
            userRepository.save(user);
        }

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/notification-preference")
    public ResponseEntity<NotificationPreferenceResponseDto> updateNotificationPreference(
            @RequestBody UpdateNotificationPreferenceRequest request,
            Principal principal) {
        String uid = principal.getName();
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setNotificationPreference(request.getPreference());

        if (request.getPreference() != NotificationPreference.TELEGRAM) {
            user.setTelegramChatId(null);
            user.setTelegramLinkToken(null);
        }

        userRepository.save(user);

        NotificationPreferenceResponseDto response = new NotificationPreferenceResponseDto();
        response.setPreference(user.getNotificationPreference());
        response.setTelegramLinked(user.getTelegramChatId() != null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/notification-preference")
    public ResponseEntity<NotificationPreferenceResponseDto> getNotificationPreference(Principal principal) {
        String uid = principal.getName();
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        NotificationPreferenceResponseDto response = new NotificationPreferenceResponseDto();
        response.setPreference(user.getNotificationPreference());
        response.setTelegramLinked(user.getTelegramChatId() != null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/telegram-link")
    public ResponseEntity<TelegramLinkResponseDto> generateTelegramLink(Principal principal) {
        String uid = principal.getName();
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String token = java.util.UUID.randomUUID().toString();
        user.setTelegramLinkToken(token);
        userRepository.save(user);
        TelegramLinkResponseDto response = new TelegramLinkResponseDto();
        response.setDeepLink("https://t.me/FreeRoom_Notifier_bot?start=" + token);
        return ResponseEntity.ok(response);
    }
}
