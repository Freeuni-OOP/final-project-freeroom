package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(user("uid-a", "nchap24@freeuni.edu.ge", "Nikoloz Chapidze"));
        userRepository.save(user("uid-b", "jfree67@freeuni.edu.ge", "John FreeUni"));
        userRepository.save(user("uid-c", "nkala24@freeuni.edu.ge", "Nikala"));
    }

    @Test
    void searchByDisplayName_findsPartialMatch() {
        List<User> results = userRepository.searchByDisplayName(
                "nik", "uid-x", PageRequest.of(0, 10));

        assertThat(results).hasSize(2);
    }

    @Test
    void searchByDisplayName_isCaseInsensitive() {
        List<User> results = userRepository.searchByDisplayName(
                "NIKOLOZ", "uid-x", PageRequest.of(0, 10));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDisplayName()).isEqualTo("Nikoloz Chapidze");
    }

    @Test
    void searchByDisplayName_excludesCurrentUser() {
        List<User> results = userRepository.searchByDisplayName(
                "nik", "uid-a", PageRequest.of(0, 10));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo("uid-c");
    }

    @Test
    void searchByDisplayName_respectsPageLimit() {
        List<User> results = userRepository.searchByDisplayName(
                "nik", "uid-x", PageRequest.of(0, 1));

        assertThat(results).hasSize(1);
    }

    @Test
    void searchByDisplayName_noMatch_returnsEmpty() {
        List<User> results = userRepository.searchByDisplayName(
                "zzz", "uid-x", PageRequest.of(0, 10));

        assertThat(results).isEmpty();
    }

    private User user(String id, String email, String displayName) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setDisplayName(displayName);
        return u;
    }
}