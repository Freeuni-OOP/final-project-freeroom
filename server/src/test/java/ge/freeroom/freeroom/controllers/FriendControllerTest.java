package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.service.FriendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendService friendService;

    @Test
    void removeFriend_ReturnsOk_AndDelegatesToService() throws Exception {
        mockMvc.perform(delete("/friends/uid-b").principal(() -> "uid-a"))
                .andExpect(status().isOk());

        verify(friendService).removeFriend("uid-a", "uid-b");
    }

    @Test
    void cancelFriendRequest_ReturnsOk_AndDelegatesToService() throws Exception {
        mockMvc.perform(delete("/friends/requests/uid-b").principal(() -> "uid-a"))
                .andExpect(status().isOk());

        verify(friendService).cancelFriendRequest("uid-a", "uid-b");
    }
}