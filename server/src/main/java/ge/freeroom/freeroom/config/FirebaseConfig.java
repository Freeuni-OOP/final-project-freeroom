package ge.freeroom.freeroom.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            FirebaseOptions options;

            java.io.InputStream serviceAccount = null;
            java.io.File secretFile = new java.io.File("secrets/serviceAccountKey.json");
            if (secretFile.exists()) {
                serviceAccount = new java.io.FileInputStream(secretFile);
            } else {
                serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
            }
            
            try {
                if (serviceAccount != null) {
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                } else {
                    System.out.println("Warning: serviceAccountKey.json not found. Falling back to Application Default Credentials.");
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.getApplicationDefault())
                            .build();
                }
            } finally {
                if (serviceAccount != null) {
                    serviceAccount.close();
                }
            }

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK initialized successfully!");
            }

        } catch (Exception e) {
            System.err.println("Notice: Firebase Admin SDK could not initialize. (Safe to ignore in CI/CD pipelines). " + e.getMessage());
        }
    }
}