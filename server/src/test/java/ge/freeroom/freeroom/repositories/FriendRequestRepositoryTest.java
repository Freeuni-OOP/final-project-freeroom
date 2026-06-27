package ge.freeroom.freeroom.repositories;

import ge.freeroom.freeroom.entities.FriendRequest;
import ge.freeroom.freeroom.entities.FriendRequestStatus;
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
class FriendRequestRepositoryTest {

    @Autowired FriendRequestRepository friendRequestRepository;
    @Autowired UserRepository userRepository;

    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        userA = savedUser("uid-a", "Zabbbb@freeuni.edu.ge", "Zabuza");
        userB = savedUser("uid-b", "dane@freeuni.edu.ge", "DaneOlmo");
        userC = savedUser("uid-c", "chapo@freeuni.edu.ge", "LiverpulisFani123");
    }

    @Test
    void findPendingBetweenUsers_findsRequestInForwardDirection() {
        save(userA, userB, FriendRequestStatus.PENDING);

        Optional<FriendRequest> result =
                friendRequestRepository.findPendingBetweenUsers("uid-a", "uid-b");

        assertThat(result).isPresent();
    }

    @Test
    void findPendingBetweenUsers_findsRequestInReverseDirection() {
        save(userB, userA, FriendRequestStatus.PENDING);

        Optional<FriendRequest> result =
                friendRequestRepository.findPendingBetweenUsers("uid-a", "uid-b");

        assertThat(result).isPresent();
    }

    @Test
    void findPendingBetweenUsers_doesNotReturnRejected() {
        save(userA, userB, FriendRequestStatus.REJECTED);

        Optional<FriendRequest> result =
                friendRequestRepository.findPendingBetweenUsers("uid-a", "uid-b");

        assertThat(result).isEmpty();
    }

    @Test
    void findPendingBetweenUsers_noRequestBetweenPair_returnsEmpty() {
        save(userA, userC, FriendRequestStatus.PENDING);

        Optional<FriendRequest> result =
                friendRequestRepository.findPendingBetweenUsers("uid-a", "uid-b");

        assertThat(result).isEmpty();
    }

    @Test
    void findPendingRequestsInvolvingUser_returnsBothSentAndReceived() {
        save(userA, userB, FriendRequestStatus.PENDING);
        save(userC, userA, FriendRequestStatus.PENDING);

        List<FriendRequest> result =
                friendRequestRepository.findPendingRequestsInvolvingUser("uid-a");

        assertThat(result).hasSize(2);
    }

    @Test
    void findPendingRequestsInvolvingUser_excludesNonPending() {
        save(userA, userB, FriendRequestStatus.REJECTED);
        save(userA, userC, FriendRequestStatus.PENDING);

        List<FriendRequest> result =
                friendRequestRepository.findPendingRequestsInvolvingUser("uid-a");

        assertThat(result).hasSize(1);
    }

    @Test
    void findByReceiverIdAndStatus_returnsIncomingPending() {
        save(userA, userB, FriendRequestStatus.PENDING);
        save(userC, userB, FriendRequestStatus.PENDING);
        save(userA, userC, FriendRequestStatus.PENDING);

        List<FriendRequest> incoming =
                friendRequestRepository.findByReceiverIdAndStatus("uid-b", FriendRequestStatus.PENDING);

        assertThat(incoming).hasSize(2);
        assertThat(incoming).allMatch(r -> r.getReceiver().getId().equals("uid-b"));
    }

    @Test
    void findBySenderIdAndStatus_returnsOutgoingPending() {
        save(userA, userB, FriendRequestStatus.PENDING);
        save(userA, userC, FriendRequestStatus.PENDING);
        save(userB, userA, FriendRequestStatus.PENDING);

        List<FriendRequest> outgoing =
                friendRequestRepository.findBySenderIdAndStatus("uid-a", FriendRequestStatus.PENDING);

        assertThat(outgoing).hasSize(2);
        assertThat(outgoing).allMatch(r -> r.getSender().getId().equals("uid-a"));
    }

    private User savedUser(String id, String email, String displayName) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setDisplayName(displayName);
        return userRepository.save(u);
    }

    private void save(User sender, User receiver, FriendRequestStatus status) {
        FriendRequest r = new FriendRequest();
        r.setSender(sender);
        r.setReceiver(receiver);
        r.setStatus(status);
        friendRequestRepository.save(r);
    }
}