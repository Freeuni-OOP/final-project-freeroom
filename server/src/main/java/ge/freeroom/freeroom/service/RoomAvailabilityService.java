package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.*;
import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.entities.NotificationPreference;
import ge.freeroom.freeroom.entities.Room;
import ge.freeroom.freeroom.entities.RoomOccupancy;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.LectureRepository;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.repositories.RoomRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ChatService chatService;

    public RoomAvailabilityService(RoomRepository roomRepository, LectureRepository lectureRepository, UserRepository userRepository, RoomOccupancyRepository roomOccupancyRepository) {
        this.roomRepository = roomRepository;
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
        this.roomOccupancyRepository = roomOccupancyRepository;
    }

    public List<RoomMapDto> getAllRoomsMap(String currentUserId){
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
                rosd.setIsMyOccupancy(occupancy.getUser().getId().equals(currentUserId));

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

        if (user.getNotificationPreference() == NotificationPreference.NONE) {
            throw new IllegalStateException("შეტყობინების მეთოდი არ არის არჩეული. პროფილზე აირჩიეთ Email ან Telegram.");
        }
        if (user.getNotificationPreference() == NotificationPreference.TELEGRAM && user.getTelegramChatId() == null) {
            throw new IllegalStateException("Telegram არ არის დაკავშირებული. პროფილზე დაასრულეთ Telegram-ის დაყენება.");
        }

        // Updated to use the pessimistic lock method to prevent dual-booking gaps
        Room room = roomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not Found"));

        LocalDateTime nowTime = LocalDateTime.now();

        Optional<RoomOccupancy> existingUserOccupancy = roomOccupancyRepository
                .findActiveOccupancyByUserId(userId, nowTime);
        if (existingUserOccupancy.isPresent()) {
            RoomOccupancy existing = existingUserOccupancy.get();
            String formattedTime = existing.getExpectedEndAt().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            throw new IllegalStateException(
                    "თქვენ უკვე დაჯავშნილი გაქვთ ოთახი " +
                            existing.getRoom().getRoomNumber() + " " +
                            formattedTime + "-მდე"
            );
        }

        List<Lecture> activeLectures = lectureRepository.findActiveLecturesByRoomIds(List.of(roomId), nowTime);
        if (!activeLectures.isEmpty()) {
            throw new IllegalStateException("Room has an active lecture");
        }

        Optional<RoomOccupancy> existing = roomOccupancyRepository.findFirstByRoomIdAndEndAtIsNull(roomId);
        if (existing.isPresent()) {
            RoomOccupancy occ = existing.get();
            if (occ.getExpectedEndAt().isAfter(nowTime)) {
                throw new IllegalStateException("room is already occupied");
            } else {
                occ.setEndAt(occ.getExpectedEndAt());
                roomOccupancyRepository.save(occ);
                if (chatService != null) {
                    chatService.clearRoomChat(roomId);
                }
            }
        }

        long minutes = (durationMinutes != null) ? durationMinutes : 60;

        RoomOccupancy occupancy = new RoomOccupancy();
        occupancy.setRoom(room);
        occupancy.setUser(user);
        occupancy.setStartAt(nowTime);
        occupancy.setExpectedEndAt(nowTime.plusMinutes(minutes));
        occupancy.setEndAt(null);

        RoomOccupancy saved = roomOccupancyRepository.save(occupancy);

        ReserveRoomResponseDto response = new ReserveRoomResponseDto();
        response.setId(saved.getId());
        response.setRoomId(saved.getRoom().getId());
        response.setRoomNumber(saved.getRoom().getRoomNumber());
        response.setStartTime(saved.getStartAt());
        response.setExpectedEndTime(saved.getExpectedEndAt());

        if (chatService != null) {
            chatService.initializeRoomBooker(roomId, userId);
        }

        return response;
    }

    @Transactional
    public CancelOccupancyResponseDto cancelOccupancy(String userId, Long roomId) {
        LocalDateTime now = LocalDateTime.now();

        Optional<RoomOccupancy> occupancyOpt = roomOccupancyRepository
                .findFirstByRoomIdAndEndAtIsNull(roomId);

        if (occupancyOpt.isEmpty()) {
            throw new IllegalStateException("No active occupancy for this room");
        }

        RoomOccupancy occ = occupancyOpt.get();

        if (occ.getExpectedEndAt().isBefore(now) || occ.getExpectedEndAt().isEqual(now)) {
            occ.setEndAt(occ.getExpectedEndAt());
            roomOccupancyRepository.save(occ);
            if (chatService != null) {
                chatService.clearRoomChat(roomId);
            }
            throw new IllegalStateException("No active occupancy for this room");
        }

        if (!occ.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You can only cancel your own occupancy");
        }

        occ.setEndAt(LocalDateTime.now());
        roomOccupancyRepository.save(occ);

        CancelOccupancyResponseDto response = new CancelOccupancyResponseDto();
        response.setOccupancyId(occ.getId());
        response.setRoomId(occ.getRoom().getId());
        response.setRoomNumber(occ.getRoom().getRoomNumber());
        response.setCancelledAt(occ.getEndAt());
        response.setMessage("Occupancy cancelled successfully");

        if (chatService != null) {
            chatService.clearRoomChat(roomId);
        }

        return response;
    }
}