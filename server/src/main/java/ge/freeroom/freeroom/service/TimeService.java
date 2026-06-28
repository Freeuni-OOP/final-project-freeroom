package ge.freeroom.freeroom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TimeService {

    @Value("${app.dev.mock-time.enabled:false}")
    private boolean mockTimeEnabled;

    @Value("${app.dev.mock-time.datetime:2026-06-02T14:50:00}")
    private String mockDateTime;

    public LocalDateTime now() {
        if (mockTimeEnabled) {
            return LocalDateTime.parse(mockDateTime);
        }
        return LocalDateTime.now();
    }
}
