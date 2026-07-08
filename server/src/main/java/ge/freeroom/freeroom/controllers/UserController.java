package ge.freeroom.freeroom.controllers;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.config.AdminUsersConfig;
import ge.freeroom.freeroom.dto.*;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.FriendshipRepository;
import ge.freeroom.freeroom.service.UserService;
import ge.freeroom.freeroom.service.TimeService;
import ge.freeroom.freeroom.security.RateLimiter;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.websocket.RealtimeEventPublisher;
import ge.freeroom.freeroom.websocket.dto.RoomEventDto;
import ge.freeroom.freeroom.websocket.events.RoomEventType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.security.Principal;
import java.util.Set;
import java.util.List;
import ge.freeroom.freeroom.entities.Subject;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RateLimiter rateLimiter;
    private final AdminUsersConfig adminUsersConfig;
    private final RoomOccupancyRepository roomOccupancyRepository;
    private final TimeService timeService;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final FriendshipRepository friendshipRepository;

    public UserController(UserService userService, UserRepository userRepository, RateLimiter rateLimiter,
                          AdminUsersConfig adminUsersConfig, RoomOccupancyRepository roomOccupancyRepository,
                          TimeService timeService, RealtimeEventPublisher realtimeEventPublisher, FriendshipRepository friendshipRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.rateLimiter = rateLimiter;
        this.adminUsersConfig = adminUsersConfig;
        this.roomOccupancyRepository = roomOccupancyRepository;
        this.timeService = timeService;
        this.realtimeEventPublisher = realtimeEventPublisher;
        this.friendshipRepository = friendshipRepository;
    }

    @GetMapping
    public ResponseEntity<UserProfileDto> getUser(HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        User user = userService.getOrCreateUser(token);
        return ResponseEntity.ok(toProfileDto(user));
    }

    @PostMapping("/sync")
    public ResponseEntity<UserProfileDto> syncUser(HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        User user = userService.getOrCreateUser(token);
        return ResponseEntity.ok(toProfileDto(user));
    }

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserProfileDto> updateUser(
            HttpServletRequest request,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "bio", required = false) String bio,
            @RequestParam(value = "file", required = false) MultipartFile file) throws Exception {

        if (bio != null && bio.length() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bio cannot exceed 300 characters");
        }

        if (bio != null) {
            bio = Jsoup.clean(bio, Safelist.none());
        }

        if (displayName != null) {
            displayName = Jsoup.clean(displayName, Safelist.none());
        }

        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");

        if (!rateLimiter.allow("profile:" + token.getUid(), 10, 60000)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        User updatedUser = userService.updateUserProfile(token.getUid(), displayName, bio, file);
        return ResponseEntity.ok(toProfileDto(updatedUser));
    }

    private UserProfileDto toProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setDisplayName(user.getDisplayName());
        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setBio(user.getBio());
        dto.setAdmin(adminUsersConfig.isAdmin(user.getEmail()));
        dto.setOccupancyVisibility(user.getOccupancyVisibility());

        roomOccupancyRepository.findActiveOccupancyByUserId(user.getId(), timeService.now())
                .ifPresent(occ -> {
                    if (occ.getRoom() != null) {
                        dto.setActiveRoomNumber(occ.getRoom().getRoomNumber());
                    }
                });

        return dto;
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

    @PatchMapping("/occupancy-visibility")
    public ResponseEntity<OccupancyVisibilityResponseDto> updateOccupancyVisibility(
            @RequestBody UpdateOccupancyVisibilityRequestDto request,
            Principal principal) {
        if (!rateLimiter.allow("pref:" + principal.getName(), 10, 60000)) {
            return ResponseEntity.status(429).build();
        }
        String uid = principal.getName();
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getVisibility() != null) {
            user.setOccupancyVisibility(request.getVisibility());
            userRepository.save(user);

            boolean hasActiveOccupancy = roomOccupancyRepository
                    .findActiveOccupancyByUserId(uid, timeService.now())
                    .isPresent();

            if (hasActiveOccupancy) {
                realtimeEventPublisher.publishOccupancyRipple(uid, friendshipRepository.findFriendIdsByUserId(uid));
                roomOccupancyRepository.findActiveOccupancyByUserId(uid, timeService.now())
                        .ifPresent(occ -> realtimeEventPublisher.publishRoomEvent(new RoomEventDto(
                                RoomEventType.ROOM_OCCUPANCY_UPDATED,
                                occ.getRoom().getId(),
                                occ.getRoom().getRoomNumber(),
                                occ.getRoom().getFloor().getNumber(),
                                timeService.now()
                        )));
            }
        }

        OccupancyVisibilityResponseDto response = new OccupancyVisibilityResponseDto();
        response.setVisibility(user.getOccupancyVisibility());
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
        if (!rateLimiter.allow("telegram:" + principal.getName(), 3, 60000)) {
            return ResponseEntity.status(429).build();
        }
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

    @GetMapping("/subjects")
    public ResponseEntity<Set<Subject>> getSavedSubjects(HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        return ResponseEntity.ok(userService.getSavedSubjects(token.getUid()));
    }

    @PostMapping("/subjects/{subjectId}")
    public ResponseEntity<Void> addSavedSubject(HttpServletRequest request, @PathVariable Long subjectId) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        userService.addSavedSubject(token.getUid(), subjectId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<Void> removeSavedSubject(HttpServletRequest request, @PathVariable Long subjectId) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        userService.removeSavedSubject(token.getUid(), subjectId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<LectureSummaryDto>> getUserCalendar(HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        return ResponseEntity.ok(userService.getUserCalendar(token.getUid()));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<PublicProfileDto> getPublicProfile(
            @PathVariable String userId,
            Principal principal) {
        return ResponseEntity.ok(userService.getPublicProfile(principal.getName(), userId));
    }
}