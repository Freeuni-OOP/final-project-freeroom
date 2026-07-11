package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.*;
import ge.freeroom.freeroom.service.ChatService;
import ge.freeroom.freeroom.security.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final RateLimiter rateLimiter;

    public ChatController(ChatService chatService, RateLimiter rateLimiter) {
        this.chatService = chatService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/{roomId}")
    public List<ChatMessageDto> getMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long beforeId,
            Principal principal) {
        return chatService.getMessages(roomId, beforeId, principal.getName());
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@Valid @RequestBody SendMessageRequestDto request, Principal principal) {
        if (!rateLimiter.allow("chat:" + principal.getName(), 20, 60000)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        String sanitizedMessage = request.message() != null ? Jsoup.clean(request.message(), Safelist.none()) : null;
        chatService.sendMessage(request.roomId(), principal.getName(), sanitizedMessage);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/request-join")
    public ResponseEntity<Void> requestJoin(@Valid @RequestBody JoinRoomRequestDto request, Principal principal) {
        if (!rateLimiter.allow("joinreq:" + principal.getName(), 5, 60000)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        chatService.sendJoinRequest(request.roomId(), principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/approve")
    public ResponseEntity<Void> approveUser(@Valid @RequestBody ApproveJoinRequestDto request, Principal principal) {
        chatService.approveJoinRequest(request.roomId(), principal.getName(), request.targetUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> rejectUser(@Valid @RequestBody RejectJoinRequestDto request, Principal principal) {
        chatService.rejectJoinRequest(request.roomId(), principal.getName(), request.targetUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/kick")
    public ResponseEntity<Void> kickUser(@Valid @RequestBody KickUserRequestDto request, Principal principal) {
        chatService.kickUser(request.roomId(), principal.getName(), request.targetUserId());
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @GetMapping("/{roomId}/members")
    public List<RoomMemberDto> getRoomMembers(@PathVariable Long roomId, Principal principal) {
        return chatService.getRoomMembers(roomId, principal.getName());
    }
}