package ge.freeroom.freeroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FreeroomApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreeroomApplication.class, args);
	}

}
