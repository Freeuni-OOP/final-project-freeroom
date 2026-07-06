package ge.freeroom.freeroom.controllers;

import com.google.firebase.auth.FirebaseToken;
import ge.freeroom.freeroom.config.AdminUsersConfig;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.FriendshipRepository;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.security.RateLimiter;
import ge.freeroom.freeroom.service.TimeService;
import ge.freeroom.freeroom.service.UserService;
import ge.freeroom.freeroom.websocket.RealtimeEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RateLimiter rateLimiter;

    @MockitoBean
    private AdminUsersConfig adminUsersConfig;

    @MockitoBean
    private RoomOccupancyRepository roomOccupancyRepository;

    @MockitoBean
    private TimeService timeService;

    @MockitoBean
    private RealtimeEventPublisher realtimeEventPublisher;

    @MockitoBean
    private FriendshipRepository friendshipRepository;

    private MockHttpServletRequest requestWithFirebaseToken(String uid) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        FirebaseToken token = org.mockito.Mockito.mock(FirebaseToken.class);
        when(token.getUid()).thenReturn(uid);
        request.setAttribute("firebaseToken", token);
        return request;
    }

    @Test
    void getUser_ReturnsOkAndProfileJson() throws Exception {
        User mockUser = new User();
        mockUser.setId("test-uid");
        mockUser.setEmail("test@freeuni.edu.ge");
        mockUser.setDisplayName("Test User");
        mockUser.setBio("bio");
        mockUser.setPhotoUrl("https://photo");
        mockUser.setOccupancyVisibility(null);

        when(userService.getOrCreateUser(any(FirebaseToken.class))).thenReturn(mockUser);
        when(adminUsersConfig.isAdmin("test@freeuni.edu.ge")).thenReturn(false);
        when(roomOccupancyRepository.findActiveOccupancyByUserId(eq("test-uid"), any())).thenReturn(Optional.empty());
        when(timeService.now()).thenReturn(java.time.LocalDateTime.of(2026, 7, 6, 12, 0));

        mockMvc.perform(get("/user").requestAttr("firebaseToken", requestWithFirebaseToken("test-uid").getAttribute("firebaseToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-uid"))
                .andExpect(jsonPath("$.email").value("test@freeuni.edu.ge"))
                .andExpect(jsonPath("$.displayName").value("Test User"))
                .andExpect(jsonPath("$.photoUrl").value("https://photo"))
                .andExpect(jsonPath("$.bio").value("bio"))
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    void getUser_ReturnsInternalServerError_WhenServiceFails() throws Exception {
        when(userService.getOrCreateUser(any(FirebaseToken.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/user").requestAttr("firebaseToken", requestWithFirebaseToken("test-uid").getAttribute("firebaseToken")))
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deepLink").value(org.hamcrest.Matchers.startsWith("https://t.me/FreeRoom_Notify_bot?start=")));

        verify(userRepository).save(mockUser);
    }

    @Test
    void generateTelegramLink_Returns429_WhenRateLimited() throws Exception {
        when(rateLimiter.allow(anyString(), anyInt(), anyLong())).thenReturn(false);

        mockMvc.perform(post("/user/telegram-link")
                        .principal(() -> "test-uid"))
                .andExpect(status().isTooManyRequests());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void syncUser_ReturnsOkAndProfileJson() throws Exception {
        User mockUser = new User();
        mockUser.setId("test-uid");
        mockUser.setEmail("test@freeuni.edu.ge");
        mockUser.setDisplayName("Test User");

        when(userService.getOrCreateUser(any(FirebaseToken.class))).thenReturn(mockUser);
        when(adminUsersConfig.isAdmin(any())).thenReturn(false);
        when(roomOccupancyRepository.findActiveOccupancyByUserId(eq("test-uid"), any())).thenReturn(Optional.empty());
        when(timeService.now()).thenReturn(java.time.LocalDateTime.of(2026, 7, 6, 12, 0));

        mockMvc.perform(post("/user/sync").requestAttr("firebaseToken", requestWithFirebaseToken("test-uid").getAttribute("firebaseToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-uid"))
                .andExpect(jsonPath("$.email").value("test@freeuni.edu.ge"));
    }

    @Test
    void getNotificationPreference_ReturnsPreferenceAndLinkedState() throws Exception {
        User mockUser = new User();
        mockUser.setId("test-uid");
        mockUser.setNotificationPreference(ge.freeroom.freeroom.entities.NotificationPreference.EMAIL);
        mockUser.setTelegramChatId(null);

        when(userRepository.findById("test-uid")).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/user/notification-preference")
                        .principal(() -> "test-uid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preference").value("EMAIL"))
                .andExpect(jsonPath("$.telegramLinked").value(false));
    }
}