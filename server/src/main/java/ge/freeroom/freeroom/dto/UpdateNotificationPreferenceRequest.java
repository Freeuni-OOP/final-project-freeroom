package ge.freeroom.freeroom.dto;

import ge.freeroom.freeroom.entities.NotificationPreference;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNotificationPreferenceRequest {
    private NotificationPreference preference;
}
