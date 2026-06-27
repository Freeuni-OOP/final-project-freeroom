package ge.freeroom.freeroom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.google.api.services.calendar.Calendar;
import ge.freeroom.freeroom.service.TelegramBotService;

@SpringBootTest
@ActiveProfiles("test")
class FreeroomApplicationTests {

	@MockitoBean
	private Calendar calendarClient;

	@MockitoBean
	private TelegramBotService telegramBotService;

	@Test
	void contextLoads() {
	}
}