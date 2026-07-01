package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.entities.Lecture;
import ge.freeroom.freeroom.entities.Room;
import ge.freeroom.freeroom.entities.Subject;
import ge.freeroom.freeroom.repositories.LectureRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LectureController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LectureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LectureRepository lectureRepository;

    @Test
    void searchLectures_ReturnsLectureDtoWithMappedFields() throws Exception {
        Subject subject = new Subject();
        subject.setTitle("Calculus");
        subject.setType("Lecture");
        subject.setGroupNumber("A1");

        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber(205);

        LocalDateTime start = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 15, 12, 0);

        Lecture lecture = new Lecture();
        lecture.setId(42L);
        lecture.setSubject(subject);
        lecture.setRoom(room);
        lecture.setStartAt(start);
        lecture.setEndAt(end);

        when(lectureRepository.searchLecturesChronologically(anyString()))
                .thenReturn(List.of(lecture));

        mockMvc.perform(get("/lectures/search").param("q", "Calc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].startAt").exists())
                .andExpect(jsonPath("$[0].endAt").exists())
                .andExpect(jsonPath("$[0].subject.title").value("Calculus"))
                .andExpect(jsonPath("$[0].subject.type").value("Lecture"))
                .andExpect(jsonPath("$[0].subject.groupNumber").value("A1"))
                .andExpect(jsonPath("$[0].room.roomNumber").value(205))
                .andExpect(jsonPath("$[0].eventExternalId").doesNotExist())
                .andExpect(jsonPath("$[0].fetchedAt").doesNotExist())
                .andExpect(jsonPath("$[0].recurring").doesNotExist());
    }

    @Test
    void searchLectures_NullSubjectAndRoom_DoesNotNPE() throws Exception {
        Lecture lecture = new Lecture();
        lecture.setId(99L);
        lecture.setSubject(null);
        lecture.setRoom(null);
        lecture.setStartAt(LocalDateTime.of(2026, 1, 15, 10, 0));
        lecture.setEndAt(LocalDateTime.of(2026, 1, 15, 12, 0));

        when(lectureRepository.searchLecturesChronologically(anyString()))
                .thenReturn(List.of(lecture));

        mockMvc.perform(get("/lectures/search").param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(99))
                .andExpect(jsonPath("$[0].subject").doesNotExist())
                .andExpect(jsonPath("$[0].room").doesNotExist());
    }
}
