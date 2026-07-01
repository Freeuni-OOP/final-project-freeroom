package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.config.AdminUsersConfig;
import ge.freeroom.freeroom.dto.CreateReportRequestDto;
import ge.freeroom.freeroom.dto.UserReportResponseDto;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.service.UserReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
public class UserReportController {
    private final UserReportService userReportService;
    private final UserRepository userRepository;
    private final AdminUsersConfig adminUsersConfig;

    public UserReportController(UserReportService userReportService, UserRepository userRepository, AdminUsersConfig adminUsersConfig) {
        this.userReportService = userReportService;
        this.userRepository = userRepository;
        this.adminUsersConfig = adminUsersConfig;
    }

    @PostMapping("/users/{userId}/report")
    public ResponseEntity<UserReportResponseDto> reportUser(
            @PathVariable String userId,
            @RequestBody CreateReportRequestDto request,
            Principal principal
            ) {
        String reporterId = principal.getName();
        UserReportResponseDto response = userReportService.createReport(reporterId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/reports")
    public ResponseEntity<List<UserReportResponseDto>> getPendingReports(Principal principal) {
        User currentUser = userRepository.findById(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // don't give access to others other than ADMINS
        if(!adminUsersConfig.isAdmin(currentUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return  ResponseEntity.ok(userReportService.getPendingReports());
    }
}
