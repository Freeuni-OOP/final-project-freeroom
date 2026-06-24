package ge.freeroom.freeroom.service;

import ge.freeroom.freeroom.entities.User;
import ge.freeroom.freeroom.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

@Component
public class TelegramBotService implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final String botToken;
    private final TelegramClient telegramClient;
    private final UserRepository userRepository;

    public TelegramBotService(@Value("${TELEGRAM_BOT_TOKEN}") String botToken, UserRepository userRepository) {
        this.botToken = botToken;
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.userRepository = userRepository;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    @Transactional
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        String text = update.getMessage().getText().trim();
        long chatId = update.getMessage().getChatId();

        if (!text.startsWith("/start")) {
            return;
        }

        String[] parts = text.split("\\s+", 2);
        if (parts.length < 2) {
            sendMessage(chatId, "გასააქტიურებლად გახსენით ბმული თქვენი FreeRoom პროფილიდან.");
            return;
        }

        String token = parts[1].trim();
        Optional<User> userOpt = userRepository.findByTelegramLinkToken(token);
        if (userOpt.isEmpty()) {
            sendMessage(chatId, "ბმული არასწორია ან ვადაგასულია. გთხოვთ, შექმენით ახალი პროფილის გვერდიდან.");
            return;
        }

        User user = userOpt.get();
        user.setTelegramChatId(chatId);
        user.setTelegramLinkToken(null);
        userRepository.save(user);

        sendMessage(chatId, "დაკავშირება დასრულდა. აქ მიიღებთ შეტყობინებებს ოთახის ჯავშნის შესახებ.");
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
