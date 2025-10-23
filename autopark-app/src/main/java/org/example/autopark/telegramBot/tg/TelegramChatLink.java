package org.example.autopark.telegramBot.tg;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "telegram_chat_link")
@Data @NoArgsConstructor @AllArgsConstructor
public class TelegramChatLink {
    @Id
    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;
}
