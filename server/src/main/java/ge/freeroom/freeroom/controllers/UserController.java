package ge.freeroom.freeroom.controllers;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
            @RequestParam(value = "file", required = false) MultipartFile file) {

        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        if (token == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            User updatedUser = userService.updateUserProfile(token.getUid(), displayName, bio, file);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            System.err.println("Failed to update user profile: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}