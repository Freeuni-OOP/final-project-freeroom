package ge.freeroom.freeroom.config;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdminUsersConfig {
    private static final Set<String> ADMIN_EMAILS = Set.of(
            "nchap24@freeuni.edu.ge",
            "gzaba24@freeuni.edu.ge",
            "nkala24@freeuni.edu.ge",
            "ndane24@freeuni.edu.ge",
            "lalad24@freeuni.edu.ge"
//            ,"testuser@freeuni.edu.ge"
    );

    public boolean isAdmin(String email) {
        return email != null && ADMIN_EMAILS.contains(email);
    }
}
