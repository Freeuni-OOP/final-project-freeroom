package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.entities.NotificationPreference;
import ge.freeroom.freeroom.entities.RoomOccupancy;
import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.RoomOccupancyRepository;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationScheduler {

    private final RoomOccupancyRepository roomOccupancyRepository;
    private final UserRepository userRepository;
    private final TelegramBotService telegramBotService;

    public NotificationScheduler(RoomOccupancyRepository roomOccupancyRepository,
                                 UserRepository userRepository,
                                 TelegramBotService telegramBotService) {
        this.roomOccupancyRepository = roomOccupancyRepository;
        this.userRepository = userRepository;
        this.telegramBotService = telegramBotService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendExpiryWarnings() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(10);
        List<RoomOccupancy> reservations = roomOccupancyRepository.findReservationsNeedingNotification(now, windowEnd);

        for (RoomOccupancy reservation : reservations) {
            User user = reservation.getUser();
            if (user.getNotificationPreference() == NotificationPreference.TELEGRAM && user.getTelegramChatId() != null) {
                String text = "თქვენი ოთახის ჯავშანი მთავრდება 10 წუთში. ოთახი " + reservation.getRoom().getRoomNumber();
                boolean sent = telegramBotService.sendNotification(user.getTelegramChatId(), text);
                if (sent) {
                    reservation.setNotifiedTenMin(true);
                    roomOccupancyRepository.save(reservation);
                } else {
                    user.setTelegramChatId(null);
                    user.setNotificationPreference(NotificationPreference.NONE);
                    userRepository.save(user);
                }
            } else {
                reservation.setNotifiedTenMin(true);
                roomOccupancyRepository.save(reservation);
            }
        }
    }
}
