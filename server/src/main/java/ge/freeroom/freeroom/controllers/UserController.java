package ge.freeroom.freeroom.controllers;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.dto.UserUpdateDto;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<User> getUserProfile(HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        User user = userService.getOrCreateUser(token);
        return ResponseEntity.ok(user);
    }

    @PatchMapping
    public ResponseEntity<User> updateProfile(HttpServletRequest request, @RequestBody UserUpdateDto updateDto) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        User updatedUser = userService.updateUser(token.getUid(), updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatar(
            HttpServletRequest request,
            @RequestParam(value = "file", required = true) MultipartFile file) {

        if (request.getAttribute("firebaseToken") == null) {
            return ResponseEntity.status(401).build();
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String publicUrl = userService.uploadAvatarToStorage(file);
            return ResponseEntity.ok(Map.of("publicUrl", publicUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Avatar upload sequence aborted: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}