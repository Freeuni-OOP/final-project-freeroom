package ge.freeroom.freeroom.service;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.InputStream;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Value("${supabase.service.key}")
    private String supabaseServiceKey;

    @Value("${supabase.project.ref}")
    private String projectRef;

    private final RestClient restClient = RestClient.create();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreateUser(FirebaseToken token) {
        String uid = token.getUid();
        return userRepository.findById(uid).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(uid);
            newUser.setEmail(token.getEmail());
            newUser.setDisplayName(token.getName());
            newUser.setPhotoUrl(token.getPicture());
            newUser.setBio("");
            return userRepository.save(newUser);
        });
    }

    @Transactional
    public User updateUserProfile(String uid, String displayName, String bio, MultipartFile file) throws Exception {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (file != null && !file.isEmpty()) {
            String photoUrl = uploadAvatarToStorage(file);
            user.setPhotoUrl(photoUrl);
        }

        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        if (bio != null) {
            user.setBio(bio);
        }

        return userRepository.save(user);
    }

    private String uploadAvatarToStorage(MultipartFile file) throws Exception {
        String fileExt = null;
        String contentType = null;

        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header);

            if (bytesRead >= 4 && header[0] == (byte) 0x89 && header[1] == (byte) 0x50 && header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
                fileExt = ".png";
                contentType = "image/png";
            } else if (bytesRead >= 2 && header[0] == (byte) 0xFF && header[1] == (byte) 0xD8) {
                fileExt = ".jpg";
                contentType = "image/jpeg";
            } else if (bytesRead >= 12 && header[0] == (byte) 0x52 && header[1] == (byte) 0x49 && header[2] == (byte) 0x46 && header[3] == (byte) 0x46 &&
                    header[8] == (byte) 0x57 && header[9] == (byte) 0x45 && header[10] == (byte) 0x42 && header[11] == (byte) 0x50) {
                fileExt = ".webp";
                contentType = "image/webp";
            }
        }

        if (fileExt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file signature. Only PNG, JPEG, and WEBP images are allowed.");
        }

        String fileName = System.currentTimeMillis() + fileExt;
        String targetUrl = "https://" + projectRef + ".supabase.co/storage/v1/object/avatars/" + fileName;

        ResponseEntity<Void> response = restClient.post()
                .uri(targetUrl)
                .header("Authorization", "Bearer " + supabaseServiceKey)
                .header("apikey", supabaseServiceKey)
                .contentType(MediaType.parseMediaType(contentType))
                .body(file.getBytes())
                .retrieve()
                .toBodilessEntity();

        if (response.getStatusCode().is2xxSuccessful()) {
            return "https://" + projectRef + ".supabase.co/storage/v1/object/public/avatars/" + fileName;
        } else {
            throw new RuntimeException("Supabase Storage rejected upload with status code: " + response.getStatusCode());
        }
    }
}