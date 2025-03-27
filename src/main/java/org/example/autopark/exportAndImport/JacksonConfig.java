package org.example.autopark.exportAndImport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // поддержка java.time.*
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // не сериализуем null'ы
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO8601 формат для дат
        return mapper;
    }
}
