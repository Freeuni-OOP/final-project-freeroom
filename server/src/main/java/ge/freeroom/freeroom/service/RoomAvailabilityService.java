package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.LectureSummaryDto;
import ge.freeroom.freeroom.dto.ReserveRoomResponseDto;
import ge.freeroom.freeroom.dto.RoomMapDto;
import ge.freeroom.freeroom.dto.RoomOccupancySummaryDto;
import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.entities.Room;
import ge.freeroom.freeroom.entities.RoomOccupancy;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.LectureRepository;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.repositories.RoomRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomAvailabilityService {

    private final RoomRepository roomRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final RoomOccupancyRepository roomOccupancyRepository;

    public RoomAvailabilityService(RoomRepository roomRepository, LectureRepository lectureRepository, UserRepository userRepository, RoomOccupancyRepository roomOccupancyRepository) {
        this.roomRepository = roomRepository;
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
        this.roomOccupancyRepository = roomOccupancyRepository;
    }

    public List<RoomMapDto> getAllRoomsMap(){
        List<Room> rooms = roomRepository.findAllWithFloor();

        List<Long> roomIds = rooms.stream()
                .map(Room::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        List<Lecture> activeLectures = roomIds.isEmpty() ? List.of() : lectureRepository.findActiveLecturesByRoomIds(roomIds, now);

        Map<Long, Lecture> activeLectureByRoomId = activeLectures.stream()
                .collect(Collectors.toMap(l -> l.getRoom().getId(), l -> l, (a,b) -> a));

        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();
        List<Lecture> upcomingLecturesToday = roomIds.isEmpty() ? List.of() : lectureRepository.findUpcomingLecturesTodayByRoomIds(roomIds, now, endOfDay);

        Map<Long, Lecture> nextLectureByRoomId = upcomingLecturesToday.stream()
                .collect(Collectors.toMap(l -> l.getRoom().getId(), l -> l, (a, b) -> a));

        List<RoomOccupancy> activeOccupancies = roomIds.isEmpty() ? List.of() :
                    roomOccupancyRepository.findActiveNonExpiredByRoomIds(roomIds, now);

        Map<Long, RoomOccupancy> activeOccupancyByRoomId = activeOccupancies.stream()
                .collect(Collectors.toMap(o -> o.getRoom().getId(), o -> o, (a, b) -> a));

        return rooms.stream().map(room -> {
            RoomMapDto dto = new RoomMapDto();
            dto.setId(room.getId());
            dto.setRoomNumber(room.getRoomNumber());
            dto.setCapacity(room.getCapacity());
            dto.setFloorNumber(room.getFloor().getNumber());

            Lecture lecture = activeLectureByRoomId.get(room.getId());
            RoomOccupancy occupancy = activeOccupancyByRoomId.get(room.getId());
            if (lecture != null) {
                dto.setStatus("occupied");
                dto.setCurrentOccupancy(null);

                LectureSummaryDto lsd = new LectureSummaryDto();
                lsd.setTitle(lecture.getTitle());
                lsd.setOrganizer(lecture.getOrganizer());
                lsd.setStartAt(lecture.getStartAt());
                lsd.setEndAt(lecture.getEndAt());

                dto.setCurrentLecture(lsd);
            } else if (occupancy != null) {
                dto.setStatus("occupied");
                dto.setCurrentLecture(null);

                RoomOccupancySummaryDto rosd = new RoomOccupancySummaryDto();
                rosd.setStartAt(occupancy.getStartAt());
                rosd.setExpectedEndAt(occupancy.getExpectedEndAt());
                rosd.setReserverDisplayName(occupancy.getUser().getDisplayName());

                dto.setCurrentOccupancy(rosd);
            }else {
                dto.setStatus("free");
                dto.setCurrentLecture(null);
                dto.setCurrentOccupancy(null);
            }

            Lecture nextLecture = nextLectureByRoomId.get(room.getId());
            if (nextLecture != null) {
                LectureSummaryDto nextLsd = new LectureSummaryDto();
                nextLsd.setTitle(nextLecture.getTitle());
                nextLsd.setOrganizer(nextLecture.getOrganizer());
                nextLsd.setStartAt(nextLecture.getStartAt());
                nextLsd.setEndAt(nextLecture.getEndAt());

                dto.setNextLecture(nextLsd);
            } else {
                dto.setNextLecture(null);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public ReserveRoomResponseDto reserveRoom(String userId, Long roomId, Long durationMinutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found. Please sync your account first."));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not Found"));

        LocalDateTime nowTime = LocalDateTime.now();

        List<Lecture> activeLectures = lectureRepository.findActiveLecturesByRoomIds(List.of(roomId), nowTime);
        if (!activeLectures.isEmpty()) {
            throw new IllegalStateException("Room has an active lecture");
        }

        Optional<RoomOccupancy> existing = roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(roomId, nowTime);
        if(existing.isPresent()) {
            throw new IllegalStateException("room is already occupied");
        }

        long minutes = (durationMinutes != null) ? durationMinutes : 60;

        RoomOccupancy occupancy = new RoomOccupancy();
        occupancy.setRoom(room);
        occupancy.setUser(user);
        occupancy.setStartAt(nowTime);
        occupancy.setExpectedEndAt(nowTime.plusMinutes(minutes));
        occupancy.setEndAt(null); // marks that it is active

        RoomOccupancy saved = roomOccupancyRepository.save(occupancy);

        ReserveRoomResponseDto response = new ReserveRoomResponseDto();
        response.setId(saved.getId());
        response.setRoomId(saved.getRoom().getId());
        response.setRoomNumber(saved.getRoom().getRoomNumber());
        response.setStartTime(saved.getStartAt());
        response.setExpectedEndTime(saved.getExpectedEndAt());

        return response;
    }
}
