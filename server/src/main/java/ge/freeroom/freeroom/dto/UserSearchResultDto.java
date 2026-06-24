package ge.freeroom.freeroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserSearchResultDto {
    private String id;
    private String displayName;
    private String photoUrl;

    private String relationshipStatus;
}
