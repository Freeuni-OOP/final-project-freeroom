package ge.freeroom.freeroom.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendReservationConfirmation(String toEmail, Integer roomNumber, LocalDateTime expectedEndAt) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setFrom("FreeRoom <freeroom.notify@gmail.com>");
        mail.setSubject("ოთახი დაჯავშნილია");
        mail.setText(
                "ოთახი " + roomNumber + " წარმატებით დაიჯავშნა.\n" +
                        "დაჯავშნა მოქმედია: " + expectedEndAt.toLocalTime() + "-მდე."
        );
        mailSender.send(mail);
    }

    @Async
    public void sendExpiryWarning(String toEmail, Integer roomNumber) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setFrom("FreeRoom <freeroom.notify@gmail.com>");
        mail.setSubject("დაჯავშნილი დრო იწურება");
        mail.setText(
                "ოთახი " + roomNumber + "-ის ჯავშნის დასრულებამდე დარჩენილია 10 წუთი"
        );
        mailSender.send(mail);
    }
}
