package org.example.notify.monitoringalert.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "notify.email")
public class NotifyEmailProperties {
    private boolean enabled = false;
    private String to;
    private String from;
    private String subjectPrefix = "[autopark][ALERT]";
}
