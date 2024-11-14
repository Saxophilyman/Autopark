package org.example.autopark;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

import static java.util.stream.Collectors.toList;

@SpringBootApplication
public class AutoparkApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoparkApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}


}
