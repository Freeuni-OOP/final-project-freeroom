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
    private final TimeService timeService;
    private final EmailService emailService;

    public NotificationScheduler(RoomOccupancyRepository roomOccupancyRepository,
                                 UserRepository userRepository,
                                 TelegramBotService telegramBotService,
                                 TimeService timeService, EmailService emailService) {
        this.roomOccupancyRepository = roomOccupancyRepository;
        this.userRepository = userRepository;
        this.telegramBotService = telegramBotService;
        this.timeService = timeService;
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendExpiryWarnings() {
        LocalDateTime now = timeService.now();
        LocalDateTime windowEnd = now.plusMinutes(10);
        List<RoomOccupancy> reservations = roomOccupancyRepository.findReservationsNeedingNotification(now, windowEnd);

        for (RoomOccupancy reservation : reservations) {
            User user = reservation.getUser();
            if (user.getNotificationPreference() == NotificationPreference.TELEGRAM && user.getTelegramChatId() != null) {
                String text = "თქვენი ოთახის ჯავშანი მთავრდება 10 წუთში. ოთახი " + reservation.getRoom().getRoomNumber();
                SendResult result = telegramBotService.sendNotification(user.getTelegramChatId(), text);
                if (result == SendResult.SUCCESS) {
                    reservation.setNotifiedTenMin(true);
                    roomOccupancyRepository.save(reservation);
                } else if (result == SendResult.BLOCKED) {
                    user.setTelegramChatId(null);
                    user.setNotificationPreference(NotificationPreference.NONE);
                    userRepository.save(user);
                }
            } else if(user.getNotificationPreference() == NotificationPreference.EMAIL) {
                emailService.sendExpiryWarning(
                        user.getEmail(),
                        reservation.getRoom().getRoomNumber()
                );
                reservation.setNotifiedTenMin(true);
                roomOccupancyRepository.save(reservation);
            } else {
                reservation.setNotifiedTenMin(true);
                roomOccupancyRepository.save(reservation);
            }
        }
    }
}
