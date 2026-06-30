package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.CreateReportRequestDto;
import ge.freeroom.freeroom.dto.UserReportResponseDto;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.service.UserReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
public class UserReportController {
    private static final Set<String> ADMIN_EMAILS = Set.of(
            "nchap24@freeuni.edu.ge",
            "gzaba24@freeuni.edu.ge",
            "nkala24@freeuni.edu.ge",
            "ndane24@freeuni.edu.ge",
            "lalad24@freeuni.edu.ge"
//            ,"testuser@freeuni.edu.ge"
            );

    private final UserReportService userReportService;
    private final UserRepository userRepository;

    public UserReportController(UserReportService userReportService, UserRepository userRepository) {
        this.userReportService = userReportService;
        this.userRepository = userRepository;
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
        if(!ADMIN_EMAILS.contains(currentUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return  ResponseEntity.ok(userReportService.getPendingReports());
    }
}
