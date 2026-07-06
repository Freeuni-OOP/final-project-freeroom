package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.dto.FriendDto;
import ge.freeroom.freeroom.dto.FriendRequestDto;
import ge.freeroom.freeroom.dto.UserSearchResultDto;
import ge.freeroom.freeroom.entities.*;
import ge.freeroom.freeroom.repositories.FriendRequestRepository;
import ge.freeroom.freeroom.repositories.FriendshipRepository;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import ge.freeroom.freeroom.websocket.RealtimeEventPublisher;
import ge.freeroom.freeroom.websocket.dto.FriendEventDto;
import ge.freeroom.freeroom.websocket.events.FriendEventType;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final RoomOccupancyRepository roomOccupancyRepository;
    private final TimeService timeService;
    private final RealtimeEventPublisher realtimeEventPublisher;

    public FriendService(
            FriendRequestRepository friendRequestRepository,
            FriendshipRepository friendshipRepository,
            UserRepository userRepository,
            RoomOccupancyRepository roomOccupancyRepository,
            TimeService timeService,
            RealtimeEventPublisher realtimeEventPublisher) {
        this.friendRequestRepository = friendRequestRepository;
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.roomOccupancyRepository = roomOccupancyRepository;
        this.timeService = timeService;
        this.realtimeEventPublisher = realtimeEventPublisher;
    }

    public List<UserSearchResultDto> searchUsers(String currentUserId, String query){
        if (query == null || query.trim().length() < 2) {
            throw new IllegalArgumentException("საძიებო ტექსტი მინიმუმ 2 სიმბოლოსგან უნდა შედგებოდეს");
        }
        if (query.trim().length() > 50) {
            throw new IllegalArgumentException("საძიებო ტექსტი ძალიან გრძელია");
        }

        String trimmed = query.trim();
        List<User> results = userRepository.searchByDisplayName(
                trimmed, currentUserId, PageRequest.of(0, 10));

        if (results.isEmpty()) {
            return List.of();
        }

        Set<String> friendIds = new HashSet<>(
                friendshipRepository.findFriendIdsByUserId(currentUserId));

        List<FriendRequest> pendingRequests =
                friendRequestRepository.findPendingRequestsInvolvingUser(currentUserId);

        Map<String, String> pendingDirectionByUserId = new HashMap<>();
        for (FriendRequest req : pendingRequests) {
            if (req.getSender().getId().equals(currentUserId)) {
                pendingDirectionByUserId.put(req.getReceiver().getId(), "SENT");
            } else {
                pendingDirectionByUserId.put(req.getSender().getId(), "RECEIVED");
            }
        }

        return results.stream().map(user -> {
            UserSearchResultDto dto = new UserSearchResultDto();
            dto.setId(user.getId());
            dto.setDisplayName(user.getDisplayName());
            dto.setPhotoUrl(user.getPhotoUrl());

            if (friendIds.contains(user.getId())) {
                dto.setRelationshipStatus(RelationshipStatus.FRIENDS);
            } else if ("SENT".equals(pendingDirectionByUserId.get(user.getId()))) {
                dto.setRelationshipStatus(RelationshipStatus.PENDING_SENT);
            } else if ("RECEIVED".equals(pendingDirectionByUserId.get(user.getId()))) {
                dto.setRelationshipStatus(RelationshipStatus.PENDING_RECEIVED);
            } else {
                dto.setRelationshipStatus(RelationshipStatus.NONE);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void sendFriendRequest(String senderId, String receiverId){
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("საკუთარ თავს მეგობრობის მოთხოვნა ვერ გაუგზავნით");
        }

        if (friendshipRepository.existsByUsers(senderId, receiverId)) {
            throw new IllegalStateException("თქვენ უკვე მეგობრები ხართ");
        }

        Optional<FriendRequest> existing =
                friendRequestRepository.findPendingBetweenUsers(senderId, receiverId);
        if (existing.isPresent()) {
            throw new IllegalStateException("მეგობრობის მოთხოვნა უკვე გაგზავნილია");
        }

        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("მომხმარებელი ვერ მოიძებნა"));

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);

        friendRequestRepository.save(request);

        realtimeEventPublisher.publishFriendEvent(receiverId, buildEvent(FriendEventType.REQUEST_SENT, request.getId(), sender));
    }

    @Transactional
    public void acceptFriendRequest(String currentUserId, Long requestId){
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("მოთხოვნა ვერ მოიძებნა"));

        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new AccessDeniedException("ამ მოთხოვნის მიღება თქვენ არ შეგიძლიათ");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("მოთხოვნა აღარ არის მოლოდინის რეჟიმში");
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        User sender = request.getSender();
        User receiver = request.getReceiver();
        User user1 = sender.getId().compareTo(receiver.getId()) <= 0 ? sender : receiver;
        User user2 = sender.getId().compareTo(receiver.getId()) <= 0 ? receiver : sender;

        Friendship friendship = new Friendship();
        friendship.setUser1(user1);
        friendship.setUser2(user2);
        friendshipRepository.save(friendship);

        realtimeEventPublisher.publishFriendEvent(sender.getId(), buildEvent(FriendEventType.REQUEST_ACCEPTED, request.getId(), receiver));
    }

    @Transactional
    public void rejectFriendRequest(String currentUserId, Long requestId){
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("მოთხოვნა ვერ მოიძებნა"));

        if (!request.getReceiver().getId().equals(currentUserId)) {
            throw new AccessDeniedException("ამ მოთხოვნის უარყოფა თქვენ არ შეგიძლიათ");
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("მოთხოვნა აღარ არის მოლოდინის რეჟიმში");
        }

        request.setStatus(FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);

        realtimeEventPublisher.publishFriendEvent(request.getSender().getId(), buildEvent(FriendEventType.REQUEST_REJECTED, request.getId(), request.getReceiver()));
    }

    public List<FriendDto> getFriends(String currentUserId){
        List<String> friendIds = friendshipRepository.findFriendIdsByUserId(currentUserId);
        if (friendIds.isEmpty()) {
            return List.of();
        }

        List<User> friends = userRepository.findAllById(friendIds);

        LocalDateTime now = timeService.now();
        List<RoomOccupancy> activeOccupancies =
                roomOccupancyRepository.findActiveNonExpiredByUserIds(friendIds, now);

        Map<String, RoomOccupancy> occupancyByUserId = activeOccupancies.stream()
                .collect(Collectors.toMap(
                        o -> o.getUser().getId(),
                        o -> o,
                        (a, b) -> a));

        return friends.stream().map(friend -> {
            FriendDto dto = new FriendDto();
            dto.setId(friend.getId());
            dto.setDisplayName(friend.getDisplayName());
            dto.setPhotoUrl(friend.getPhotoUrl());

            RoomOccupancy occ = occupancyByUserId.get(friend.getId());

            ge.freeroom.freeroom.entities.OccupancyVisibility visibility = friend.getOccupancyVisibility();
            if (visibility == null) visibility = ge.freeroom.freeroom.entities.OccupancyVisibility.FRIENDS;
            
            boolean canView = (visibility == ge.freeroom.freeroom.entities.OccupancyVisibility.PUBLIC) ||
                              (visibility == ge.freeroom.freeroom.entities.OccupancyVisibility.FRIENDS);

            if (occ != null && canView) {
                dto.setHasActiveOccupancy(true);
                FriendDto.OccupancyInfo info = new FriendDto.OccupancyInfo();
                info.setRoomNumber(occ.getRoom().getRoomNumber());
                info.setFloorNumber(occ.getRoom().getFloor().getNumber());
                info.setStartAt(occ.getStartAt());
                info.setExpectedEndAt(occ.getExpectedEndAt());
                dto.setCurrentOccupancy(info);
            } else {
                dto.setHasActiveOccupancy(false);
                dto.setCurrentOccupancy(null);
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public List<FriendRequestDto> getIncomingRequests(String currentUserId) {
        return friendRequestRepository
                .findByReceiverIdAndStatus(currentUserId, FriendRequestStatus.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<FriendRequestDto> getOutgoingRequests(String currentUserId) {
        return friendRequestRepository
                .findBySenderIdAndStatus(currentUserId, FriendRequestStatus.PENDING)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private FriendRequestDto toDto(FriendRequest req) {
        FriendRequestDto dto = new FriendRequestDto();
        dto.setRequestId(req.getId());
        dto.setSenderId(req.getSender().getId());
        dto.setSenderDisplayName(req.getSender().getDisplayName());
        dto.setSenderPhotoUrl(req.getSender().getPhotoUrl());
        dto.setReceiverId(req.getReceiver().getId());
        dto.setReceiverDisplayName(req.getReceiver().getDisplayName());
        dto.setReceiverPhotoUrl(req.getReceiver().getPhotoUrl());
        dto.setStatus(req.getStatus());
        dto.setCreatedAt(req.getCreatedAt());
        return dto;
    }

    @Transactional
    public void removeFriend(String currentUserId, String friendId) {
        Friendship friendship = friendshipRepository.findByUsers(currentUserId, friendId)
                .orElseThrow(() -> new IllegalStateException("თქვენ არ ხართ მეგობრები"));

        friendshipRepository.delete(friendship);

        User actor = userRepository.findById(currentUserId).orElseThrow();
        realtimeEventPublisher.publishFriendEvent(friendId, buildEvent(FriendEventType.FRIEND_REMOVED, null, actor));
    }

    @Transactional
    public void cancelFriendRequest(String currentUserId, String receiverId) {
        FriendRequest request = friendRequestRepository.findPendingBetweenUsers(currentUserId, receiverId)
                .orElseThrow(() -> new IllegalStateException("აქტიური მოთხოვნა ვერ მოიძებნა"));

        if (!request.getSender().getId().equals(currentUserId)) {
            throw new AccessDeniedException("ამ მოთხოვნის გაუქმება თქვენ არ შეგიძლიათ");
        }

        friendRequestRepository.delete(request);

        realtimeEventPublisher.publishFriendEvent(receiverId, buildEvent(FriendEventType.REQUEST_CANCELLED, request.getId(), request.getSender()));
    }

    private FriendEventDto buildEvent(FriendEventType type, Long requestId, User actor) {
        return new FriendEventDto(type, requestId, actor.getId(), actor.getDisplayName(), actor.getPhotoUrl(), timeService.now());
    }
}