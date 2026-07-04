package ge.freeroom.freeroom.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileDto {
    private String id;
    private String email;
    private String displayName;
    private String photoUrl;
    private String bio;
}
