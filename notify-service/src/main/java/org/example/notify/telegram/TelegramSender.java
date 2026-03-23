package org.example.notify.telegram;

public interface TelegramSender {
    void send(long chatId, String text);
    void sendToAlertChat(String text);
}
