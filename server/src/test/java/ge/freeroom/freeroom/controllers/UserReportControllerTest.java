package ge.freeroom.freeroom.controllers;

import tools.jackson.databind.ObjectMapper;
import ge.freeroom.freeroom.config.AdminUsersConfig;
import ge.freeroom.freeroom.dto.CreateReportRequestDto;
import ge.freeroom.freeroom.dto.UserReportResponseDto;
import ge.freeroom.freeroom.entities.ReportReason;
import ge.freeroom.freeroom.entities.ReportStatus;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.service.UserReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserReportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserReportService userReportService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AdminUsersConfig adminUsersConfig;

    @Test
    void reportUser_ReturnsCreated() throws Exception {
        CreateReportRequestDto request = new CreateReportRequestDto();
        request.setReason(ReportReason.HARASSMENT);
        request.setDetails(null);

        UserReportResponseDto response = new UserReportResponseDto();
        response.setId(1L);
        response.setReportedUserId("target-uid");
        response.setReporterUserId("reporter-uid");
        response.setReason(ReportReason.HARASSMENT);
        response.setStatus(ReportStatus.PENDING);
        response.setCreatedAt(LocalDateTime.now());

        when(userReportService.createReport(anyString(), anyString(), any(CreateReportRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/users/target-uid/report")
                        .principal(() -> "reporter-uid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportedUserId").value("target-uid"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getPendingReports_ReturnsForbidden_WhenNotAdmin() throws Exception {
        User nonAdmin = new User();
        nonAdmin.setId("regular-uid");
        nonAdmin.setEmail("regular@freeuni.edu.ge");

        when(userRepository.findById("regular-uid")).thenReturn(Optional.of(nonAdmin));
        when(adminUsersConfig.isAdmin("regular@freeuni.edu.ge")).thenReturn(false);

        mockMvc.perform(get("/admin/reports")
                        .principal(() -> "regular-uid"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPendingReports_ReturnsOk_WhenAdmin() throws Exception {
        User admin = new User();
        admin.setId("admin-uid");
        admin.setEmail("gzaba24@freeuni.edu.ge");

        UserReportResponseDto report = new UserReportResponseDto();
        report.setId(1L);
        report.setReason(ReportReason.HARASSMENT);
        report.setStatus(ReportStatus.PENDING);

        when(userRepository.findById("admin-uid")).thenReturn(Optional.of(admin));
        when(adminUsersConfig.isAdmin("gzaba24@freeuni.edu.ge")).thenReturn(true);
        when(userReportService.getPendingReports()).thenReturn(List.of(report));

        mockMvc.perform(get("/admin/reports")
                        .principal(() -> "admin-uid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getPendingReports_ReturnsBadRequest_WhenPrincipalUserNotFound() throws Exception {
        when(userRepository.findById("ghost-uid")).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/reports")
                        .principal(() -> "ghost-uid"))
                .andExpect(status().isBadRequest());
    }
}