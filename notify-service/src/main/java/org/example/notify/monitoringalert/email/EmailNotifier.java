package org.example.notify.monitoringalert.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "notify.email", name = "enabled", havingValue = "true")
public class EmailNotifier {
        
    private final JavaMailSender mailSender;
    private final NotifyEmailProperties props;

    /**
     * Отправка алерта по email.
     */
    public void sendAlert(String message) {
        // 1. Email-алерты глобально выключены
        if (!props.isEnabled()) {
            log.debug("Email alerts disabled, skip: {}", message);
            return;
        }

        // 2. Не настроены адреса
        if (props.getTo() == null || props.getFrom() == null) {
            log.warn("notify.email.to or from not configured, skip email alert: {}", message);
            return;
        }

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(props.getTo());
            mail.setFrom(props.getFrom());
            mail.setSubject(props.getSubjectPrefix() + " " + shortSubject(message));
            mail.setText(message);

            mailSender.send(mail);
            log.info("Monitoring email alert sent to {}", props.getTo());
        } catch (Exception e) {
            log.warn("Failed to send monitoring email alert", e);
        }
    }

    private String shortSubject(String message) {
        return message.length() > 60 ? message.substring(0, 60) + "..." : message;
    }
}
