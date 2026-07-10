package ge.freeroom.freeroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class FreeroomApplication {

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tbilisi"));
	}

	public static void main(String[] args) {
		SpringApplication.run(FreeroomApplication.class, args);
	}

}
