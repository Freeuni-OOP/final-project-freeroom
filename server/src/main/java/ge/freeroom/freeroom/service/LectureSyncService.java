package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.entities.Floor;
import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.entities.Room;
import ge.freeroom.freeroom.repositories.FloorRepository;
import ge.freeroom.freeroom.repositories.LectureRepository;
import ge.freeroom.freeroom.repositories.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LectureSyncService {

    private final GoogleCalendarService calendarService;
    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;
    private final LectureRepository lectureRepository;

    public LectureSyncService(GoogleCalendarService calendarService, RoomRepository roomRepository, FloorRepository floorRepository, LectureRepository lectureRepository) {
        this.calendarService = calendarService;
        this.roomRepository = roomRepository;
        this.floorRepository = floorRepository;
        this.lectureRepository = lectureRepository;
    }

    public void syncAllRooms() {
        List<Integer> roomNumbers = new ArrayList<>();
        IntStream.rangeClosed(101, 119).forEach(roomNumbers::add);
        IntStream.rangeClosed(200, 227).forEach(roomNumbers::add);
        IntStream.rangeClosed(301, 329).forEach(roomNumbers::add);
        IntStream.rangeClosed(401, 426).forEach(roomNumbers::add);

        Map<Integer, Room> roomMap = new ConcurrentHashMap<>();
        for (int roomNum : roomNumbers) {
            int floorNum = roomNum / 100;
            Floor floor = floorRepository.findByNumber(floorNum).orElseGet(() -> {
                Floor newFloor = new Floor();
                newFloor.setNumber(floorNum);
                return floorRepository.save(newFloor);
            });

            Room room = roomRepository.findByRoomNumber(roomNum).orElseGet(() -> {
                Room newRoom = new Room();
                newRoom.setRoomNumber(roomNum);
                newRoom.setCapacity(30);
                newRoom.setFloor(floor);
                return roomRepository.save(newRoom);
            });
            roomMap.put(roomNum, room);
        }

        System.out.println("--- Starting concurrent fetch for " + roomNumbers.size() + " rooms...");
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);

        List<Lecture> allFetchedLectures = roomNumbers.parallelStream()
                .flatMap(roomNum -> {
                    List<Lecture> lecs = calendarService.fetchLectures(roomNum, start, end);
                    
                    Room roomEntity = roomMap.get(roomNum);
                    lecs.forEach(lec -> lec.setRoom(roomEntity));
                    
                    return lecs.stream();
                })
                .collect(Collectors.toList());

        System.out.println("--- Finished fetching. Total lectures found: " + allFetchedLectures.size());

        System.out.println("--- Starting DB Upsert...");
        for (Lecture lec : allFetchedLectures) {
            parseLectureTitle(lec);

            Optional<Lecture> existingOpt = lectureRepository.findByEventExternalId(lec.getEventExternalId());
            if (existingOpt.isPresent()) {
                Lecture existing = existingOpt.get();
                existing.setTitle(lec.getTitle());
                existing.setDescription(lec.getDescription());
                existing.setOrganizer(lec.getOrganizer());
                existing.setStartAt(lec.getStartAt());
                existing.setEndAt(lec.getEndAt());
                existing.setRoom(lec.getRoom());
                existing.setFetchedAt(lec.getFetchedAt());
                lectureRepository.save(existing);
            } else {
                lectureRepository.save(lec);
            }
        }
        System.out.println("--- DB Upsert completed!");
    }

    /**
     * Parses the raw event title returned by Google Calendar into Lecture fields.
     * Expected Google Calendar Title Format: "Subject, (Type) - Organizer, Room"
     * Example: "F - ლოგიკა, (ლექ.) - გაბელაია დავითი, 204"
     * 
     * Breakdown:
     * - commaParts[0] = "F - ლოგიკა" (Subject -> Title)
     * - commaParts[1] = "(ლექ.) - გაბელაია დავითი"
     *   - dashParts[0] = "(ლექ.)" (Type -> Description)
     *   - dashParts[1] = "გაბელაია დავითი" (Organizer -> Organizer)
     */
    private void parseLectureTitle(Lecture lec) {
        String rawTitle = lec.getTitle();
        if (rawTitle == null || rawTitle.isEmpty()) return;

        try {
            String[] commaParts = rawTitle.split(", ");
            if (commaParts.length >= 2) {
                lec.setTitle(commaParts[0].trim());

                String[] dashParts = commaParts[1].split(" - ");
                if (dashParts.length >= 2) {
                    lec.setDescription(dashParts[0].trim());
                    lec.setOrganizer(dashParts[1].trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse title: " + rawTitle);
        }
    }
}
