package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testUpdateUserProfile_UserNotFound() {
        when(userRepository.findById("unknown-uid")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.updateUserProfile("unknown-uid", "New Name", "Bio", null);
        });
    }

    @Test
    public void testUpdateUserProfile_UpdatesFieldsCorrectly() throws Exception {
        User user = new User();
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0)); // Returns the input

        userService.updateUserProfile("user123", "New Name", "New Bio", null);

        assertEquals("New Name", user.getDisplayName());
        assertEquals("New Bio", user.getBio());
    }

    @Test
    public void testUpdateUserProfile_KeepsOldValues_WhenParamsAreNull() throws Exception {
        User existingUser = new User();
        existingUser.setDisplayName("Keep Me");
        when(userRepository.findById("user123")).thenReturn(Optional.of(existingUser));

        userService.updateUserProfile("user123", null, null, null);

        assertEquals("Keep Me", existingUser.getDisplayName());
    }
}