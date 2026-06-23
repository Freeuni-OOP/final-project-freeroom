package ge.freeroom.freeroom.service;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.dto.UserUpdateDto;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Value("${supabase.service.key}")
    private String supabaseServiceKey;

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
    public User updateUser(String uid, UserUpdateDto updateDto) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateDto.getDisplayName() != null) {
            user.setDisplayName(updateDto.getDisplayName());
        }
        if (updateDto.getPhotoUrl() != null) {
            user.setPhotoUrl(updateDto.getPhotoUrl());
        }
        if (updateDto.getBio() != null) {
            user.setBio(updateDto.getBio());
        }

        return userRepository.save(user);
    }

    public String uploadAvatarToStorage(MultipartFile file) throws Exception {
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
            return "https://" + projectRef + ".supabase.co/storage/v1/object/public/avatars/" + fileName;
        } else {
            throw new RuntimeException("Supabase Storage rejected upload with status code: " + responseCode);
        }
    }
}