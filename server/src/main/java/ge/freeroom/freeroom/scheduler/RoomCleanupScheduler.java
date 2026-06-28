package ge.freeroom.freeroom.scheduler;

import ge.freeroom.freeroom.entities.RoomOccupancy;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.service.ChatService;
import ge.freeroom.freeroom.service.TimeService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RoomCleanupScheduler {

    private final RoomOccupancyRepository roomOccupancyRepository;
    private final ChatService chatService;
    private final TimeService timeService;

    public RoomCleanupScheduler(RoomOccupancyRepository roomOccupancyRepository, ChatService chatService, TimeService timeService) {
        this.roomOccupancyRepository = roomOccupancyRepository;
        this.chatService = chatService;
        this.timeService = timeService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void purgeExpiredRooms() {
        LocalDateTime now = timeService.now();
        List<RoomOccupancy> expired = roomOccupancyRepository.findExpiredOccupancies(now);

        for (RoomOccupancy occ : expired) {
            occ.setEndAt(occ.getExpectedEndAt());
            roomOccupancyRepository.save(occ);
            if (chatService != null) {
                chatService.clearRoomChat(occ.getRoom().getId());
            }
        }
    }
}