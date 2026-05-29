package ge.freeroom.freeroom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@RestController
public class DatabaseDemo {

    @Autowired
    private EntityManager entityManager;
    // THIS IS JUST FOR TESTING DB CONNECTION, WILL BE REMOVED

    @GetMapping("/test-db")
    public String testConnection() {
        try {
            Query query = entityManager.createNativeQuery("SELECT version();");

            return (String) query.getSingleResult();
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }
}