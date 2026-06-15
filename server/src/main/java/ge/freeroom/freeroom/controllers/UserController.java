package ge.freeroom.freeroom.controllers;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.security.FirebaseTokenFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncUser(HttpServletRequest request, Principal principal) {
        FirebaseToken token = (FirebaseToken) request.getAttribute("firebaseToken");
        String uid = token.getUid();

        if (!userRepository.existsById(uid)) {
            User user = new User();
            user.setId(uid);
            user.setEmail(token.getEmail());
            user.setDisplayName(token.getName());
            user.setPhotoUrl(token.getPicture());
            userRepository.save(user);
        }

        return ResponseEntity.ok().build();
    }
}
