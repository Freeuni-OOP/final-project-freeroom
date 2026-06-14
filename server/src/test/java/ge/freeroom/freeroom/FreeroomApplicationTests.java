package ge.freeroom.freeroom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.google.api.services.calendar.Calendar;

@SpringBootTest
@ActiveProfiles("test")
class FreeroomApplicationTests {

	@MockitoBean
	private Calendar calendarClient;

	@Test
	void contextLoads() {
	}
}