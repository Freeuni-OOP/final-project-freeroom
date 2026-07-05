package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.CreateReportRequestDto;
import ge.freeroom.freeroom.dto.UserReportResponseDto;
import ge.freeroom.freeroom.entities.ReportStatus;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.entities.UserReport;
import ge.freeroom.freeroom.repositories.UserReportRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class UserReportService {
    private final UserReportRepository userReportRepository;
    private final UserRepository userRepository;

    public UserReportService(UserReportRepository userReportRepository, UserRepository userRepository) {
        this.userReportRepository = userReportRepository;
        this.userRepository = userRepository;
    }

    public UserReportResponseDto createReport(String reporterId, String reportedUserId, CreateReportRequestDto request) {
        if(reporterId.equals(reportedUserId)) {
            throw new IllegalArgumentException("you cannot report yourself");
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found"));

        User reportedUser = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Reported User not found"));

        if(userReportRepository.existsByReportedUser_IdAndReporterUser_Id(reportedUserId, reporterId)) {
            throw new IllegalStateException("You have already reported this user");
        }

        UserReport report = new UserReport();
        report.setReportedUser(reportedUser);
        report.setReporterUser(reporter);
        report.setReason(request.getReason());
        report.setDetails(request.getDetails());
        report.setStatus(ReportStatus.PENDING);

        UserReport savedReport = userReportRepository.save(report);
        return toDto(savedReport);
    }

    public List<UserReportResponseDto> getPendingReports() {
        return userReportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private UserReportResponseDto toDto(UserReport report) {
        UserReportResponseDto dto = new UserReportResponseDto();
        dto.setId(report.getId());
        dto.setReportedUserId(report.getReportedUser().getId());
        dto.setReportedUserDisplayName(report.getReportedUser().getDisplayName());
        dto.setReporterUserId(report.getReporterUser().getId());
        dto.setReporterUserDisplayName(report.getReporterUser().getDisplayName());
        dto.setReason(report.getReason());
        dto.setDetails(report.getDetails());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }
}
