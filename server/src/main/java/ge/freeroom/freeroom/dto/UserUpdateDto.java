package ge.freeroom.freeroom.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {
    private String displayName;
    private String photoUrl;
    private String bio;
}