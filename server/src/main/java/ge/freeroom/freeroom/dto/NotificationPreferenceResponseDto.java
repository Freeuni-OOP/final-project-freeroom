package ge.freeroom.freeroom.dto;

import ge.freeroom.freeroom.entities.NotificationPreference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationPreferenceResponseDto {
    private NotificationPreference preference;
    private boolean telegramLinked;
}
