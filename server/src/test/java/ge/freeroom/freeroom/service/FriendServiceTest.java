package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.FriendDto;
import ge.freeroom.freeroom.dto.FriendRequestDto;
import ge.freeroom.freeroom.dto.UserSearchResultDto;
import ge.freeroom.freeroom.entities.*;
import ge.freeroom.freeroom.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock FriendRequestRepository friendRequestRepository;
    @Mock FriendshipRepository friendshipRepository;
    @Mock UserRepository userRepository;
    @Mock RoomOccupancyRepository roomOccupancyRepository;
    @Mock TimeService timeService;

    @InjectMocks FriendService friendService;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        userA = new User();
        userA.setId("uid-a");
        userA.setDisplayName("AladasGPT");
        userA.setPhotoUrl("https://photo.a");

        userB = new User();
        userB.setId("uid-b");
        userB.setDisplayName("HakeriKala");
        userB.setPhotoUrl("https://photo.k");
    }

    @Test
    void searchUsers_tooShort_throws() {
        assertThatThrownBy(() -> friendService.searchUsers("uid-a", "a"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchUsers_tooLong_throws() {
        assertThatThrownBy(() -> friendService.searchUsers("uid-a", "a".repeat(51)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchUsers_noResults_returnsEmptyList() {
        when(userRepository.searchByDisplayName(any(), any(), any(Pageable.class)))
                .thenReturn(List.of());

        List<UserSearchResultDto> result = friendService.searchUsers("uid-a", "xyz");

        assertThat(result).isEmpty();
        verifyNoInteractions(friendshipRepository);
        verifyNoInteractions(friendRequestRepository);
    }

    @Test
    void searchUsers_noRelationship_returnsNone() {
        when(userRepository.searchByDisplayName(any(), any(), any(Pageable.class)))
                .thenReturn(List.of(userB));
        when(friendshipRepository.findFriendIdsByUserId("uid-a")).thenReturn(List.of());
        when(friendRequestRepository.findPendingRequestsInvolvingUser("uid-a")).thenReturn(List.of());

        List<UserSearchResultDto> result = friendService.searchUsers("uid-a", "Bo");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationshipStatus()).isEqualTo(RelationshipStatus.NONE);
    }

    @Test
    void searchUsers_alreadyFriends_returnsFriends() {
        when(userRepository.searchByDisplayName(any(), any(), any(Pageable.class)))
                .thenReturn(List.of(userB));
        when(friendshipRepository.findFriendIdsByUserId("uid-a")).thenReturn(List.of("uid-b"));
        when(friendRequestRepository.findPendingRequestsInvolvingUser("uid-a")).thenReturn(List.of());

        List<UserSearchResultDto> result = friendService.searchUsers("uid-a", "Bo");

        assertThat(result.get(0).getRelationshipStatus()).isEqualTo(RelationshipStatus.FRIENDS);
    }

    @Test
    void searchUsers_pendingSent_returnsPendingSent() {
        FriendRequest pending = new FriendRequest();
        pending.setSender(userA);
        pending.setReceiver(userB);
        pending.setStatus(FriendRequestStatus.PENDING);

        when(userRepository.searchByDisplayName(any(), any(), any(Pageable.class)))
                .thenReturn(List.of(userB));
        when(friendshipRepository.findFriendIdsByUserId("uid-a")).thenReturn(List.of());
        when(friendRequestRepository.findPendingRequestsInvolvingUser("uid-a"))
                .thenReturn(List.of(pending));

        List<UserSearchResultDto> result = friendService.searchUsers("uid-a", "Bo");

        assertThat(result.get(0).getRelationshipStatus()).isEqualTo(RelationshipStatus.PENDING_SENT);
    }

    @Test
    void searchUsers_pendingReceived_returnsPendingReceived() {
        FriendRequest pending = new FriendRequest();
        pending.setSender(userB);
        pending.setReceiver(userA);
        pending.setStatus(FriendRequestStatus.PENDING);

        when(userRepository.searchByDisplayName(any(), any(), any(Pageable.class)))
                .thenReturn(List.of(userB));
        when(friendshipRepository.findFriendIdsByUserId("uid-a")).thenReturn(List.of());
        when(friendRequestRepository.findPendingRequestsInvolvingUser("uid-a"))
                .thenReturn(List.of(pending));

        List<UserSearchResultDto> result = friendService.searchUsers("uid-a", "Bo");

        assertThat(result.get(0).getRelationshipStatus()).isEqualTo(RelationshipStatus.PENDING_RECEIVED);
    }

    @Test
    void sendFriendRequest_toSelf_throws() {
        assertThatThrownBy(() -> friendService.sendFriendRequest("uid-a", "uid-a"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sendFriendRequest_receiverNotFound_throws() {
        when(userRepository.findById("uid-a")).thenReturn(Optional.of(userA));
        when(userRepository.findById("uid-b")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.sendFriendRequest("uid-a", "uid-b"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sendFriendRequest_alreadyFriends_throws() {
        when(friendshipRepository.existsByUsers("uid-a", "uid-b")).thenReturn(true);

        assertThatThrownBy(() -> friendService.sendFriendRequest("uid-a", "uid-b"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sendFriendRequest_pendingExists_throws() {
        when(friendshipRepository.existsByUsers("uid-a", "uid-b")).thenReturn(false);
        when(friendRequestRepository.findPendingBetweenUsers("uid-a", "uid-b"))
                .thenReturn(Optional.of(new FriendRequest()));

        assertThatThrownBy(() -> friendService.sendFriendRequest("uid-a", "uid-b"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sendFriendRequest_valid_savesRequest() {
        when(friendshipRepository.existsByUsers("uid-a", "uid-b")).thenReturn(false);
        when(friendRequestRepository.findPendingBetweenUsers("uid-a", "uid-b"))
                .thenReturn(Optional.empty());
        when(userRepository.findById("uid-a")).thenReturn(Optional.of(userA));
        when(userRepository.findById("uid-b")).thenReturn(Optional.of(userB));

        friendService.sendFriendRequest("uid-a", "uid-b");

        verify(friendRequestRepository).save(argThat(r ->
                r.getSender().getId().equals("uid-a") &&
                        r.getReceiver().getId().equals("uid-b") &&
                        r.getStatus() == FriendRequestStatus.PENDING
        ));
    }

    @Test
    void acceptFriendRequest_notFound_throws() {
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.acceptFriendRequest("uid-b", 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptFriendRequest_wrongReceiver_throwsAccessDenied() {
        FriendRequest req = pendingRequest(userA, userB);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> friendService.acceptFriendRequest("uid-c", 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void acceptFriendRequest_notPending_throws() {
        FriendRequest req = pendingRequest(userA, userB);
        req.setStatus(FriendRequestStatus.REJECTED);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> friendService.acceptFriendRequest("uid-b", 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void acceptFriendRequest_valid_createsFriendshipAndUpdatesStatus() {
        FriendRequest req = pendingRequest(userA, userB);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        friendService.acceptFriendRequest("uid-b", 1L);

        assertThat(req.getStatus()).isEqualTo(FriendRequestStatus.ACCEPTED);
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    void acceptFriendRequest_normalizesUserOrder() {
        FriendRequest req = pendingRequest(userB, userA);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        friendService.acceptFriendRequest("uid-a", 1L);

        verify(friendshipRepository).save(argThat(f ->
                f.getUser1().getId().compareTo(f.getUser2().getId()) < 0
        ));
    }

    @Test
    void rejectFriendRequest_wrongReceiver_throwsAccessDenied() {
        FriendRequest req = pendingRequest(userA, userB);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> friendService.rejectFriendRequest("uid-c", 1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectFriendRequest_valid_setsRejected() {
        FriendRequest req = pendingRequest(userA, userB);
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(req));

        friendService.rejectFriendRequest("uid-b", 1L);

        assertThat(req.getStatus()).isEqualTo(FriendRequestStatus.REJECTED);
        verify(friendRequestRepository).save(req);
        verifyNoInteractions(friendshipRepository);
    }

    @Test
    void getFriends_noFriends_returnsEmptyList() {
        when(friendshipRepository.findFriendIdsByUserId("uid-a")).thenReturn(List.of());

        assertThat(friendService.getFriends("uid-a")).isEmpty();
        verifyNoInteractions(userRepository, roomOccupancyRepository);
    }

    @Test
    void getFriends_friendWithNoOccupancy_returnsCorrectDto() {
        when(friendshipRepository.findFriendIdsByUserId("uid-a")).thenReturn(List.of("uid-b"));
        when(userRepository.findAllById(List.of("uid-b"))).thenReturn(List.of(userB));
        when(roomOccupancyRepository.findActiveNonExpiredByUserIds(anyList(), any()))
                .thenReturn(List.of());

        List<FriendDto> friends = friendService.getFriends("uid-a");

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo("uid-b");
        assertThat(friends.get(0).isHasActiveOccupancy()).isFalse();
        assertThat(friends.get(0).getCurrentOccupancy()).isNull();
    }

    @Test
    void getIncomingRequests_returnsOnlyPendingForCurrentUser() {
        FriendRequest req = pendingRequest(userA, userB);
        when(friendRequestRepository.findByReceiverIdAndStatus("uid-b", FriendRequestStatus.PENDING))
                .thenReturn(List.of(req));

        List<FriendRequestDto> result = friendService.getIncomingRequests("uid-b");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSenderId()).isEqualTo("uid-a");
        assertThat(result.get(0).getReceiverId()).isEqualTo("uid-b");
    }

    @Test
    void getOutgoingRequests_returnsOnlyPendingFromCurrentUser() {
        FriendRequest req = pendingRequest(userA, userB);
        when(friendRequestRepository.findBySenderIdAndStatus("uid-a", FriendRequestStatus.PENDING))
                .thenReturn(List.of(req));

        List<FriendRequestDto> result = friendService.getOutgoingRequests("uid-a");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSenderId()).isEqualTo("uid-a");
    }

    private FriendRequest pendingRequest(User sender, User receiver) {
        FriendRequest req = new FriendRequest();
        req.setSender(sender);
        req.setReceiver(receiver);
        req.setStatus(FriendRequestStatus.PENDING);
        return req;
    }

    @Test
    void removeFriend_notFriends_throws() {
        when(friendshipRepository.findByUsers("uid-a", "uid-b")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.removeFriend("uid-a", "uid-b"))
                .isInstanceOf(IllegalStateException.class);

        verify(friendshipRepository, never()).delete(any(Friendship.class));
    }

    @Test
    void removeFriend_valid_deletesFriendship() {
        Friendship friendship = new Friendship();
        friendship.setUser1(userA);
        friendship.setUser2(userB);
        when(friendshipRepository.findByUsers("uid-a", "uid-b")).thenReturn(Optional.of(friendship));

        friendService.removeFriend("uid-a", "uid-b");

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void cancelFriendRequest_noPendingRequest_throws() {
        when(friendRequestRepository.findPendingBetweenUsers("uid-a", "uid-b")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.cancelFriendRequest("uid-a", "uid-b"))
                .isInstanceOf(IllegalStateException.class);

        verify(friendRequestRepository, never()).delete(any(FriendRequest.class));
    }

    @Test
    void cancelFriendRequest_callerIsNotSender_throwsAccessDenied() {
        FriendRequest req = pendingRequest(userB, userA);
        when(friendRequestRepository.findPendingBetweenUsers("uid-a", "uid-b")).thenReturn(Optional.of(req));

        assertThatThrownBy(() -> friendService.cancelFriendRequest("uid-a", "uid-b"))
                .isInstanceOf(AccessDeniedException.class);

        verify(friendRequestRepository, never()).delete(any(FriendRequest.class));
    }

    @Test
    void cancelFriendRequest_callerIsSender_deletesRequest() {
        FriendRequest req = pendingRequest(userA, userB);
        when(friendRequestRepository.findPendingBetweenUsers("uid-a", "uid-b")).thenReturn(Optional.of(req));

        friendService.cancelFriendRequest("uid-a", "uid-b");

        verify(friendRequestRepository).delete(req);
    }
}