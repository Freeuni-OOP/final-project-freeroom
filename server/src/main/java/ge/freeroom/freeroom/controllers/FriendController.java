package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.FriendDto;
import ge.freeroom.freeroom.dto.FriendRequestDto;
import ge.freeroom.freeroom.dto.SendFriendRequestDto;
import ge.freeroom.freeroom.dto.UserSearchResultDto;
import ge.freeroom.freeroom.security.RateLimiter;
import ge.freeroom.freeroom.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/friends")
public class FriendController {
    private final FriendService friendService;
    private final RateLimiter rateLimiter;

    public FriendController(FriendService friendService, RateLimiter rateLimiter) {
        this.friendService = friendService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDto>> searchUsers(
            @RequestParam String q,
            Principal principal) {
        return ResponseEntity.ok(friendService.searchUsers(principal.getName(), q));
    }

    @PostMapping("/requests")
    public ResponseEntity<Void> sendFriendRequest(
            @RequestBody SendFriendRequestDto body,
            Principal principal) {
        if (!rateLimiter.allow("friendreq:" + principal.getName(), 10, 60000)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        friendService.sendFriendRequest(principal.getName(), body.getReceiverId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendRequestDto>> getIncomingRequests(Principal principal) {
        return ResponseEntity.ok(friendService.getIncomingRequests(principal.getName()));
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendRequestDto>> getOutgoingRequests(Principal principal) {
        return ResponseEntity.ok(friendService.getOutgoingRequests(principal.getName()));
    }

    @PatchMapping("/requests/{requestId}/accept")
    public ResponseEntity<Void> acceptRequest(
            @PathVariable Long requestId,
            Principal principal) {
        friendService.acceptFriendRequest(principal.getName(), requestId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectRequest(
            @PathVariable Long requestId,
            Principal principal) {
        friendService.rejectFriendRequest(principal.getName(), requestId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<FriendDto>> getFriends(Principal principal) {
        return ResponseEntity.ok(friendService.getFriends(principal.getName()));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable String friendId,
            Principal principal) {
        friendService.removeFriend(principal.getName(), friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/requests/{userId}")
    public ResponseEntity<Void> cancelFriendRequest(
            @PathVariable String userId,
            Principal principal) {
        friendService.cancelFriendRequest(principal.getName(), userId);
        return ResponseEntity.ok().build();
    }
}