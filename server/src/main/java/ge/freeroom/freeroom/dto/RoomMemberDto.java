package ge.freeroom.freeroom.dto;

public record RoomMemberDto(
        String id,
        String nickname,
        String photoUrl,
        boolean isAdmin
) {}