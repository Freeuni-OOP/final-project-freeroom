package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.entities.Floor;
import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.entities.Room;
import ge.freeroom.freeroom.entities.Subject;
import ge.freeroom.freeroom.repositories.FloorRepository;
import ge.freeroom.freeroom.repositories.LectureRepository;
import ge.freeroom.freeroom.repositories.RoomRepository;
import ge.freeroom.freeroom.repositories.SubjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LectureSyncService {

    private final GoogleCalendarService calendarService;
    private final RoomRepository roomRepository;
    private final FloorRepository floorRepository;
    private final LectureRepository lectureRepository;
    private final SubjectRepository subjectRepository;

    public LectureSyncService(GoogleCalendarService calendarService, RoomRepository roomRepository, FloorRepository floorRepository, LectureRepository lectureRepository, SubjectRepository subjectRepository) {
        this.calendarService = calendarService;
        this.roomRepository = roomRepository;
        this.floorRepository = floorRepository;
        this.lectureRepository = lectureRepository;
        this.subjectRepository = subjectRepository;
    }

    public void syncAllRooms() {
        List<Integer> roomNumbers = getTargetRoomNumbers();
        Map<Integer, Room> roomMap = getOrCreateRooms(roomNumbers);

        System.out.println("--- Starting concurrent fetch for " + roomNumbers.size() + " rooms...");
        List<Lecture> allFetchedLectures = fetchLecturesForRooms(roomNumbers, roomMap);
        System.out.println("--- Finished fetching. Total lectures found: " + allFetchedLectures.size());

        saveLecturesAndSubjects(allFetchedLectures);
    }

    private List<Integer> getTargetRoomNumbers() {
        List<Integer> roomNumbers = new ArrayList<>();
        addRoomRange(roomNumbers, 101, 119);
        addRoomRange(roomNumbers, 200, 227);
        addRoomRange(roomNumbers, 301, 329);
        addRoomRange(roomNumbers, 401, 426);
        return roomNumbers;
    }

    private void addRoomRange(List<Integer> rooms, int start, int end) {
        IntStream.rangeClosed(start, end).forEach(rooms::add);
    }

    private Map<Integer, Room> getOrCreateRooms(List<Integer> roomNumbers) {
        Map<Integer, Room> roomMap = new ConcurrentHashMap<>();
        for (int roomNum : roomNumbers) {
            Floor floor = getOrCreateFloor(roomNum / 100);
            Room room = getOrCreateRoom(roomNum, floor);
            roomMap.put(roomNum, room);
        }
        return roomMap;
    }

    private Floor getOrCreateFloor(int floorNum) {
        return floorRepository.findByNumber(floorNum).orElseGet(() -> {
            Floor newFloor = new Floor();
            newFloor.setNumber(floorNum);
            return floorRepository.save(newFloor);
        });
    }

    private Room getOrCreateRoom(int roomNum, Floor floor) {
        return roomRepository.findByRoomNumber(roomNum).orElseGet(() -> {
            Room newRoom = new Room();
            newRoom.setRoomNumber(roomNum);
            newRoom.setCapacity(30);
            newRoom.setFloor(floor);
            return roomRepository.save(newRoom);
        });
    }

    private List<Lecture> fetchLecturesForRooms(List<Integer> roomNumbers, Map<Integer, Room> roomMap) {
        LocalDateTime start = LocalDate.of(2026, 6, 2).atStartOfDay();
        LocalDateTime end = LocalDate.of(2026, 6, 2).atTime(23, 59, 59);

        return roomNumbers.parallelStream()
                .flatMap(roomNum -> {
                    List<Lecture> lecs = calendarService.fetchLectures(roomNum, start, end);
                    Room roomEntity = roomMap.get(roomNum);
                    lecs.forEach(lec -> lec.setRoom(roomEntity));
                    return lecs.stream();
                })
                .collect(Collectors.toList());
    }

    private void saveLecturesAndSubjects(List<Lecture> allFetchedLectures) {
        System.out.println("--- Starting DB Truncate & Insert...");
        for (Lecture lec : allFetchedLectures) {
            lec.setSubject(getOrCreateSubject(lec.getSubject()));
        }

        lectureRepository.deleteAllInBatch();
        lectureRepository.saveAll(allFetchedLectures);
        System.out.println("--- DB Truncate & Insert completed!");
    }

    private Subject getOrCreateSubject(Subject parsedSubject) {
        return subjectRepository.findFirstByTitleAndTypeAndGroupNumberAndLecturer(
                parsedSubject.getTitle(), 
                parsedSubject.getType(), 
                parsedSubject.getGroupNumber(), 
                parsedSubject.getLecturer()
        ).orElseGet(() -> subjectRepository.save(parsedSubject));
    }

    public static Subject parseSubject(String rawTitle) {
        if (rawTitle == null || rawTitle.isBlank()) return new Subject();

        String cleanedTitle = stripTrailingRoom(rawTitle);
        return extractSubjectParts(cleanedTitle);
    }
    
    public static String stripTrailingRoom(String title) {
        int lastComma = title.lastIndexOf(", ");
        if (lastComma != -1 && title.substring(lastComma + 2).trim().length() < 10) {
            return title.substring(0, lastComma);
        }
        return title;
    }

    public static Subject extractSubjectParts(String text) {
        Subject subject = new Subject();
        subject.setLecturer("Unknown Organizer");

        int firstComma = text.indexOf(", ");
        if (firstComma == -1) {
            subject.setTitle(text);
            return subject;
        }

        subject.setTitle(text.substring(0, firstComma).trim());
        String rest = text.substring(firstComma + 2).trim();
        
        int lastDash = rest.lastIndexOf(" - ");
        if (lastDash != -1) {
            subject.setLecturer(rest.substring(lastDash + 3).trim());
            String typeAndGroup = rest.substring(0, lastDash).trim();
            
            int tgComma = typeAndGroup.indexOf(", ");
            if (tgComma != -1) {
                subject.setType(typeAndGroup.substring(0, tgComma).trim());
                subject.setGroupNumber(typeAndGroup.substring(tgComma + 2).trim());
            } else {
                subject.setType(typeAndGroup);
            }
        } else {
            subject.setType(rest);
        }

        return subject;
    }
}
