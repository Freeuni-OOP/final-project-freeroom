package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.ChatMessageDto;
import ge.freeroom.freeroom.dto.SendMessageRequestDto;
import ge.freeroom.freeroom.dto.JoinRoomRequestDto;
import ge.freeroom.freeroom.dto.ApproveJoinRequestDto;
import ge.freeroom.freeroom.dto.RejectJoinRequestDto;
import ge.freeroom.freeroom.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/{roomId}")
    public List<ChatMessageDto> getMessages(@PathVariable Long roomId, Principal principal) {
        return chatService.getMessages(roomId, principal.getName());
    }

    @PostMapping("/send")
    public void sendMessage(@Valid @RequestBody SendMessageRequestDto request, Principal principal) {
        chatService.sendMessage(request.roomId(), principal.getName(), request.message());
    }

    @PostMapping("/request-join")
    public void requestJoin(@Valid @RequestBody JoinRoomRequestDto request, Principal principal) {
        chatService.sendJoinRequest(request.roomId(), principal.getName());
    }

    @PostMapping("/approve")
    public void approveUser(@Valid @RequestBody ApproveJoinRequestDto request, Principal principal) {
        chatService.approveJoinRequest(request.roomId(), principal.getName(), request.targetUserId());
    }

    @PostMapping("/reject")
    public void rejectUser(@Valid @RequestBody RejectJoinRequestDto request, Principal principal) {
        chatService.rejectJoinRequest(request.roomId(), principal.getName(), request.targetUserId());
    }
}