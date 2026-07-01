package ge.freeroom.freeroom.dto;

import java.time.LocalDateTime;

public record LectureDto(
        Long id,
        LocalDateTime startAt,
        LocalDateTime endAt,
        SubjectSummaryDto subject,
        RoomSummaryDto room
) {}
