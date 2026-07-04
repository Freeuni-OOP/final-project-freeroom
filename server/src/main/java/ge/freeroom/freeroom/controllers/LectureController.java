package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.LectureDto;
import ge.freeroom.freeroom.dto.RoomSummaryDto;
import ge.freeroom.freeroom.dto.SubjectSummaryDto;
import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.entities.Subject;
import ge.freeroom.freeroom.repositories.LectureRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(produces = "application/json")
public class LectureController {

    private final LectureRepository lectureRepository;

    public LectureController(LectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }

    @GetMapping("/lectures")
    public List<LectureDto> getAllLectures() {
        return lectureRepository.findAll().stream().map(this::toDto).toList();
    }

    @GetMapping("/lectures/floor/{floorNumber}")
    public List<LectureDto> getLecturesByFloor(@PathVariable int floorNumber) {
        return lectureRepository.findByRoomFloorNumberOrderByStartAtAsc(floorNumber).stream().map(this::toDto).toList();
    }

    @GetMapping("/lectures/room/{roomNumber}")
    public List<LectureDto> getLecturesByRoom(@PathVariable Integer roomNumber) {
        return lectureRepository.findByRoomRoomNumberOrderByStartAtAsc(roomNumber).stream().map(this::toDto).toList();
    }

    @GetMapping("/lectures/search")
    public List<LectureDto> searchLectures(@RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        String safeSearchTerm = "%" + query.trim() + "%";
        return lectureRepository.searchLecturesChronologically(safeSearchTerm)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private LectureDto toDto(Lecture lecture) {
        Subject s = lecture.getSubject();
        SubjectSummaryDto subject = s != null
                ? new SubjectSummaryDto(s.getTitle(), s.getType(), s.getGroupNumber())
                : null;
        RoomSummaryDto room = lecture.getRoom() != null
                ? new RoomSummaryDto(lecture.getRoom().getRoomNumber())
                : null;
        return new LectureDto(lecture.getId(), lecture.getStartAt(), lecture.getEndAt(), subject, room);
    }
}