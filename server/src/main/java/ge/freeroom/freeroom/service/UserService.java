package ge.freeroom.freeroom.service;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.dto.UserUpdateDto;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

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
}