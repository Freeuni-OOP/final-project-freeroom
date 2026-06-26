package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

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
}
