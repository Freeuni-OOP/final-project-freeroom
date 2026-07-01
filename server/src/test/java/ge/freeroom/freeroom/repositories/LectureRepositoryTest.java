package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Floor;
import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.entities.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LectureRepositoryTest {

    @Autowired LectureRepository lectureRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired FloorRepository floorRepository;

    private Room roomA;
    private Room roomB;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        Floor floor = new Floor();
        floor.setId(1L);
        floor.setNumber(1);
        floor = floorRepository.save(floor);

        roomA = savedRoom(1L, 101, floor);
        roomB = savedRoom(2L, 102, floor);

        now = LocalDateTime.of(2026, 6, 2, 14, 50, 0);
    }

    @Test
    void findNextLecturesByRoomId_returnsOnlyLecturesAfterNow() {
        save(roomA, now.minusMinutes(30), now.minusMinutes(5));
        save(roomA, now.plusMinutes(45), now.plusMinutes(90));

        List<Lecture> result = lectureRepository.findNextLecturesByRoomId(roomA.getId(), now);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartAt()).isEqualTo(now.plusMinutes(45));
    }

    @Test
    void findNextLecturesByRoomId_excludesLectureStartingExactlyNow() {
        save(roomA, now, now.plusMinutes(60));

        List<Lecture> result = lectureRepository.findNextLecturesByRoomId(roomA.getId(), now);

        assertThat(result).isEmpty();
    }

    @Test
    void findNextLecturesByRoomId_ordersByStartAtAscending() {
        save(roomA, now.plusMinutes(120), now.plusMinutes(150));
        save(roomA, now.plusMinutes(30), now.plusMinutes(60));
        save(roomA, now.plusMinutes(75), now.plusMinutes(90));

        List<Lecture> result = lectureRepository.findNextLecturesByRoomId(roomA.getId(), now);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getStartAt()).isEqualTo(now.plusMinutes(30));
        assertThat(result.get(1).getStartAt()).isEqualTo(now.plusMinutes(75));
        assertThat(result.get(2).getStartAt()).isEqualTo(now.plusMinutes(120));
    }

    @Test
    void findNextLecturesByRoomId_scopedToGivenRoomOnly() {
        save(roomA, now.plusMinutes(30), now.plusMinutes(60));
        save(roomB, now.plusMinutes(20), now.plusMinutes(50));

        List<Lecture> result = lectureRepository.findNextLecturesByRoomId(roomA.getId(), now);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoom().getId()).isEqualTo(roomA.getId());
    }

    @Test
    void findNextLecturesByRoomId_noUpcomingLectures_returnsEmpty() {
        List<Lecture> result = lectureRepository.findNextLecturesByRoomId(roomA.getId(), now);

        assertThat(result).isEmpty();
    }

    private Room savedRoom(Long id, int roomNumber, Floor floor) {
        Room room = new Room();
        room.setId(id);
        room.setRoomNumber(roomNumber);
        room.setCapacity(30);
        room.setFloor(floor);
        return roomRepository.save(room);
    }

    private int lectureCounter = 0;

    private void save(Room room, LocalDateTime startAt, LocalDateTime endAt) {
        Lecture l = new Lecture();
        l.setRoom(room);
        l.setStartAt(startAt);
        l.setEndAt(endAt);
        l.setEventExternalId("test-event-" + (++lectureCounter));
        lectureRepository.save(l);
    }
}