package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.repositories.LectureRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public List<Lecture> getAllLectures() {
        return lectureRepository.findAll();
    }

    @GetMapping("/lectures/floor/{floorNumber}")
    public List<Lecture> getLecturesByFloor(@PathVariable int floorNumber) {
        return lectureRepository.findByRoomFloorNumberOrderByStartAtAsc(floorNumber);
    }

    @GetMapping("/lectures/room/{roomNumber}")
    public List<Lecture> getLecturesByRoom(@PathVariable Integer roomNumber) {
        return lectureRepository.findByRoomRoomNumberOrderByStartAtAsc(roomNumber);
    }
}
