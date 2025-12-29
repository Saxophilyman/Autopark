package org.example.autopark.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Profile("!reactive")
@EnableJpaRepositories(basePackages = {
        "org.example.autopark.repository", // пакет с JPA репами
        "org.example.autopark.simpleuser", //
        "org.example.autopark.trip", //
        "org.example.autopark.gps",
        "org.example.autopark.exportAndImport"//
}, bootstrapMode = org.springframework.data.repository.config.BootstrapMode.DEFERRED // <-- важное
)
@EntityScan(basePackages = {
        "org.example.autopark.entity",    // пакет с сущностями JPA
        "org.example.autopark.GPS",        // если GpsPoint – JPA сущность
        "org.example.autopark.simpleUser",        //
        "org.example.autopark.trip",
        "org.example.autopark.exportAndImport"//
})
public class JpaConfig {
}
