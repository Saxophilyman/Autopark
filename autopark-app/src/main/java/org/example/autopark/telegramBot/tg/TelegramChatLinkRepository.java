package org.example.autopark.telegramBot.tg;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramChatLinkRepository extends JpaRepository<TelegramChatLink, Long> {
}
