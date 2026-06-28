package ge.freeroom.freeroom.controllers;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.dto.NotificationPreferenceResponseDto;
import ge.freeroom.freeroom.dto.TelegramLinkResponseDto;
import ge.freeroom.freeroom.dto.UpdateNotificationPreferenceRequest;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.service.UserService;
import ge.freeroom.freeroom.security.RateLimiter;
import ge.freeroom.freeroom.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RateLimiter rateLimiter;

    public UserController(UserService userService, UserRepository userRepository, RateLimiter rateLimiter) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping
    public ResponseEntity<User> getUser(HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        User user = userService.getOrCreateUser(token);
        return ResponseEntity.ok(user);
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUser(
            HttpServletRequest request,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        User updatedUser = userService.updateUserProfile(token.getUid(), displayName, bio, file);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/notification-preference")
    public ResponseEntity<NotificationPreferenceResponseDto> updateNotificationPreference(
            @RequestBody UpdateNotificationPreferenceRequest request,
            Principal principal) {
        if (!rateLimiter.allow("pref:" + principal.getName(), 10, 60000)) {
            return ResponseEntity.status(429).build();
        }
        String uid = principal.getName();
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setNotificationPreference(request.getPreference());
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
        response.setDeepLink("https://t.me/FreeRoom_Notify_bot?start=" + token);
        return ResponseEntity.ok(response);
    }
}