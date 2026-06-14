package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.LectureSummaryDto;
import ge.freeroom.freeroom.dto.RoomMapDto;
import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.entities.Room;
import ge.freeroom.freeroom.repositories.LectureRepository;
import ge.freeroom.freeroom.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomAvailabilityService {

    private final RoomRepository roomRepository;
    private final LectureRepository lectureRepository;

    public RoomAvailabilityService(RoomRepository roomRepository, LectureRepository lectureRepository) {
        this.roomRepository = roomRepository;
        this.lectureRepository = lectureRepository;
    }

    public List<RoomMapDto> getAllRoomsMap(){
        List<Room> rooms = roomRepository.findAllWithFloor();

        List<Long> roomIds = rooms.stream()
                .map(Room::getId)
                .collect(Collectors.toList());

//        LocalDateTime now = LocalDateTime.now();
        LocalDateTime now = LocalDateTime.of(2026, 6, 15, 16, 0);
        List<Lecture> activeLectures = roomIds.isEmpty() ? List.of() : lectureRepository.findActiveLecturesByRoomIds(roomIds, now);

        Map<Long, Lecture> activeLectureByRoomId = activeLectures.stream()
                .collect(Collectors.toMap(l -> l.getRoom().getId(), l -> l, (a,b) -> a));

        return rooms.stream().map(room -> {
            RoomMapDto dto = new RoomMapDto();
            dto.setId(room.getId());
            dto.setRoomNumber(room.getRoomNumber());
            dto.setCapacity(room.getCapacity());
            dto.setFloorNumber(room.getFloor().getNumber());

            Lecture lecture = activeLectureByRoomId.get(room.getId());
            if (lecture != null) {
                dto.setStatus("occupied");
                LectureSummaryDto lsd = new LectureSummaryDto();
                lsd.setTitle(lecture.getTitle());
                lsd.setOrganizer(lecture.getOrganizer());
                lsd.setStartAt(lecture.getStartAt());
                lsd.setEndAt(lecture.getEndAt());
                dto.setCurrentLecture(lsd);
            } else {
                dto.setStatus("free");
                dto.setCurrentLecture(null);
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
