package org.example.autopark;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.List;
import java.util.TimeZone;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy
public class AutoparkApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // Принудительно устанавливаем UTC
		SpringApplication.run(AutoparkApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}


}
