package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.service.FriendService;
import ge.freeroom.freeroom.service.UserService;
import ge.freeroom.freeroom.security.RateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RateLimiter rateLimiter;

    @Test
    void getUser_ReturnsOk() throws Exception {
        User mockUser = new User();
        mockUser.setId("test-uid");

        when(userService.getOrCreateUser(any())).thenReturn(mockUser);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk());
    }


    @Test
    void getUser_ReturnsUserJson() throws Exception {
        User mockUser = new User();
        mockUser.setId("test-uid");
        when(userService.getOrCreateUser(any())).thenReturn(mockUser);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-uid"));
    }

    @Test
    void getUser_ReturnsInternalServerError_WhenServiceFails() throws Exception {
        when(userService.getOrCreateUser(any())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/user"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void generateTelegramLink_ReturnsOk_WhenNotRateLimited() throws Exception {
        when(rateLimiter.allow(anyString(), anyInt(), anyLong())).thenReturn(true);

        User mockUser = new User();
        mockUser.setId("test-uid");
        when(userRepository.findById("test-uid")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        mockMvc.perform(post("/user/telegram-link")
                        .principal(() -> "test-uid"))
                .andExpect(status().isOk());
    }

    @Test
    void generateTelegramLink_Returns429_WhenRateLimited() throws Exception {
        when(rateLimiter.allow(anyString(), anyInt(), anyLong())).thenReturn(false);

        mockMvc.perform(post("/user/telegram-link")
                        .principal(() -> "test-uid"))
                .andExpect(status().isTooManyRequests());

        verify(userRepository, never()).save(any(User.class));
    }
}
