package ge.freeroom.freeroom.controllers;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.dto.UserUpdateDto;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.security.FirebaseTokenFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<User> syncUser(HttpServletRequest request, Principal principal) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        String uid = token.getUid();

        User user = userRepository.findById(uid).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(uid);
            newUser.setEmail(token.getEmail());
            newUser.setDisplayName(token.getName());
            newUser.setPhotoUrl(token.getPicture());
            return userRepository.save(newUser);
        });

        return ResponseEntity.ok(user);
    }

    @PostMapping("/update")
    public ResponseEntity<User> updateProfile(HttpServletRequest request, @RequestBody UserUpdateDto updateDto) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        String uid = token.getUid();

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

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(HttpServletRequest request) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        String uid = token.getUid();

        User user = userRepository.findById(uid).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(uid);
            newUser.setEmail(token.getEmail());
            newUser.setDisplayName(token.getName());
            newUser.setPhotoUrl(token.getPicture());
            newUser.setBio("");
            return userRepository.save(newUser);
        });

        System.out.println("====== BACKEND PROFILE FETCH LOG ======");
        System.out.println("User ID: " + user.getId());
        System.out.println("User Bio directly from DB: '" + user.getBio() + "'");
        System.out.println("=======================================");

        return ResponseEntity.ok(user);
    }

}
