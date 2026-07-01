package ge.freeroom.freeroom.dto;

import ge.freeroom.freeroom.entities.RelationshipStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicProfileDto {
    private String id;
    private String displayName;
    private String photoUrl;
    private String bio;
    private RelationshipStatus relationshipStatus;
}