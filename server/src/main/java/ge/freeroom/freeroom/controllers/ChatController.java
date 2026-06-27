package ge.freeroom.freeroom.controllers;

import ge.freeroom.freeroom.dto.ChatRequest;
import ge.freeroom.freeroom.entities.Chat;
import ge.freeroom.freeroom.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/{roomId}")
    public List<Chat> getMessages(@PathVariable Long roomId, Principal principal) {
        return chatService.getMessages(roomId, principal.getName());
    }

    @PostMapping("/send")
    public void sendMessage(@RequestBody ChatRequest request, Principal principal) {
        chatService.sendMessage(request.getRoomId(), principal.getName(), request.getMessage());
    }

    @PostMapping("/request-join")
    public void requestJoin(@RequestBody ChatRequest request, Principal principal) {
        chatService.sendJoinRequest(request.getRoomId(), principal.getName());
    }

    @PostMapping("/approve")
    public void approveUser(@RequestBody ChatRequest request, Principal principal) {
        chatService.approveJoinRequest(request.getRoomId(), principal.getName(), request.getTargetUserId());
    }
}