package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void sendMessage_ReturnsOk_WhenValid() throws Exception {
        mockMvc.perform(post("/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomId\":1,\"message\":\"hello\"}")
                        .principal(() -> "test-uid"))
                .andExpect(status().isOk());

        verify(chatService).sendMessage(1L, "test-uid", "hello");
    }

    @Test
    void sendMessage_ReturnsBadRequest_WhenMessageTooLong() throws Exception {
        String longMessage = "a".repeat(2001);
        String json = "{\"roomId\":1,\"message\":\"" + longMessage + "\"}";

        mockMvc.perform(post("/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .principal(() -> "test-uid"))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).sendMessage(anyLong(), anyString(), anyString());
    }
}
