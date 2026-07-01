package ge.freeroom.freeroom.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import ge.freeroom.freeroom.entities.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponseDto {
    @JsonUnwrapped
    private User user;
    private boolean isAdmin;

    public UserResponseDto(User user, boolean isAdmin) {
        this.user = user;
        this.isAdmin = isAdmin;
    }
}
