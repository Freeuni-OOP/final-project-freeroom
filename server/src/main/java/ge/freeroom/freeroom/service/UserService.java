package ge.freeroom.freeroom.service;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

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
        String originalName = file.getOriginalFilename();
        String fileExt = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
        String fileName = System.currentTimeMillis() + fileExt;

        String targetUrl = "https://" + projectRef + ".supabase.co/storage/v1/object/avatars/" + fileName;

        ResponseEntity<Void> response = restClient.post()
                .uri(targetUrl)
                .header("Authorization", "Bearer " + supabaseServiceKey)
                .header("apikey", supabaseServiceKey)
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .body(file.getBytes()) // RestClient handles Content-Length automatically
                .retrieve()
                .toBodilessEntity();

        if (response.getStatusCode().is2xxSuccessful()) {
            return "https://" + projectRef + ".supabase.co/storage/v1/object/public/avatars/" + fileName;
        } else {
            throw new RuntimeException("Supabase Storage rejected upload with status code: " + response.getStatusCode());
        }
    }
}