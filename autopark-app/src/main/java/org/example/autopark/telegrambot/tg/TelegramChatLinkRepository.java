package org.example.autopark.telegrambot.tg;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramChatLinkRepository extends JpaRepository<TelegramChatLink, Long> {
}
