package ge.freeroom.freeroom.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleCalendarConfig {
    @Value("${google.calendar.credentials.path}")
    private String credentialsPath;

    @Bean
    public Calendar calendarClient() throws IOException, GeneralSecurityException {
        System.out.println("---- Creating Google Calendar bean");
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ClassPathResource(credentialsPath).getInputStream())
                .createScoped(Collections.singleton(CalendarScopes.CALENDAR_READONLY));
//                .createDelegated("admin@university.edu"); // gotta get admin privileges to calendar

        HttpRequestInitializer reqInitializer = new HttpCredentialsAdapter(credentials);

        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                reqInitializer)
                .setApplicationName("FreeRoom")
                .build();
    }
}
