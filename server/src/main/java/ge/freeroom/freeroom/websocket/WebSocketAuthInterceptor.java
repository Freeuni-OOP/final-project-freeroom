package ge.freeroom.freeroom.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Principal user = accessor.getUser();

            if (destination != null && destination.startsWith("/topic/users/") && destination.endsWith("/friends")) {
                String uid = user != null ? user.getName() : null;
                if (uid == null || !destination.equals("/topic/users/" + uid + "/friends")) {
                    throw new IllegalArgumentException("Not authorized to subscribe to this destination");
                }
            }
        }
        return message;
    }
}