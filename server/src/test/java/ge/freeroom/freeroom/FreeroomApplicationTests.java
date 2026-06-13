package ge.freeroom.freeroom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.google.api.services.calendar.Calendar;

@SpringBootTest
@ActiveProfiles("test")
class FreeroomApplicationTests {

	@MockBean
	private Calendar calendarClient;

	@Test
	void contextLoads() {
	}
}