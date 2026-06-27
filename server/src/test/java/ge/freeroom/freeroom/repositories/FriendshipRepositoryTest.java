package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.Friendship;
import ge.freeroom.freeroom.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FriendshipRepositoryTest {

    @Autowired FriendshipRepository friendshipRepository;
    @Autowired UserRepository userRepository;

    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        userA = savedUser("uid-a", "alada@freeuni.edu.ge", "aladin");
        userB = savedUser("uid-b", "shubo@freeuni.edu.ge", "shubli");
        userC = savedUser("uid-c", "chap@freeuni.edu.ge", "darwizzy");
    }

    @Test
    void existsByUsers_returnsTrueWhenFriends() {
        saveFriendship(userA, userB);

        assertThat(friendshipRepository.existsByUsers("uid-a", "uid-b")).isTrue();
    }

    @Test
    void existsByUsers_worksInReverseOrder() {
        saveFriendship(userA, userB);

        assertThat(friendshipRepository.existsByUsers("uid-b", "uid-a")).isTrue();
    }

    @Test
    void existsByUsers_returnsFalseWhenNotFriends() {
        saveFriendship(userA, userC);

        assertThat(friendshipRepository.existsByUsers("uid-a", "uid-b")).isFalse();
    }

    @Test
    void findFriendIdsByUserId_returnsAllFriendIds() {
        saveFriendship(userA, userB);
        saveFriendship(userA, userC);

        List<String> friendIds = friendshipRepository.findFriendIdsByUserId("uid-a");

        assertThat(friendIds).containsExactlyInAnyOrder("uid-b", "uid-c");
    }

    @Test
    void findFriendIdsByUserId_worksWhenUserIsUser2() {
        saveFriendship(userA, userC);

        List<String> friendIds = friendshipRepository.findFriendIdsByUserId("uid-c");

        assertThat(friendIds).containsExactly("uid-a");
    }

    @Test
    void findFriendIdsByUserId_noFriends_returnsEmpty() {
        List<String> friendIds = friendshipRepository.findFriendIdsByUserId("uid-a");

        assertThat(friendIds).isEmpty();
    }

    @Test
    void findByUsers_returnsCorrectFriendship() {
        saveFriendship(userA, userB);

        Optional<Friendship> result = friendshipRepository.findByUsers("uid-a", "uid-b");

        assertThat(result).isPresent();
    }

    @Test
    void findByUsers_worksInReverseOrder() {
        saveFriendship(userA, userB);

        Optional<Friendship> result = friendshipRepository.findByUsers("uid-b", "uid-a");

        assertThat(result).isPresent();
    }


    private User savedUser(String id, String email, String displayName) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setDisplayName(displayName);
        return userRepository.save(u);
    }

    private void saveFriendship(User u1, User u2) {
        User first = u1.getId().compareTo(u2.getId()) <= 0 ? u1 : u2;
        User second = u1.getId().compareTo(u2.getId()) <= 0 ? u2 : u1;

        Friendship f = new Friendship();
        f.setUser1(first);
        f.setUser2(second);
        friendshipRepository.save(f);
    }
}