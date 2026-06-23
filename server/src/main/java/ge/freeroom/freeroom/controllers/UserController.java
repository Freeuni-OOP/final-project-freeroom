package ge.freeroom.freeroom.controllers;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.dto.UserUpdateDto;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    @Value("${supabase.service.key}")
    private String supabaseServiceKey;

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

        try {
            if (request.getAttribute("firebaseToken") == null) {
                return ResponseEntity.status(401).build();
            }

            if (file.isEmpty()) {
                System.err.println("Upload attempted with an empty multipart file payload.");
                return ResponseEntity.badRequest().build();
            }

            String projectRef = "lahucjwdhglaxwdkiroz";
            String originalName = file.getOriginalFilename();
            String fileExt = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String fileName = System.currentTimeMillis() + fileExt;

            String targetUrl = "https://" + projectRef + ".supabase.co/storage/v1/object/avatars/" + fileName;

            URL url = new URL(targetUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Authorization", "Bearer " + supabaseServiceKey);
            connection.setRequestProperty("apikey", supabaseServiceKey);
            connection.setRequestProperty("Content-Type", file.getContentType());
            connection.setRequestProperty("Content-Length", String.valueOf(file.getSize()));

            try (OutputStream os = connection.getOutputStream()) {
                os.write(file.getBytes());
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                String publicUrl = "https://" + projectRef + ".supabase.co/storage/v1/object/public/avatars/" + fileName;
                return ResponseEntity.ok(Map.of("publicUrl", publicUrl));
            } else {
                System.err.println("Supabase Storage rejected stream with status code: " + responseCode);
                return ResponseEntity.status(responseCode).build();
            }

        } catch (Exception e) {
            System.err.println("Avatar stream transmission failed: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}