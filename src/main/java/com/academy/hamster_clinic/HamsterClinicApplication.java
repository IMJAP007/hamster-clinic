package com.academy.hamster_clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;


@OpenAPIDefinition(
		info = @Info(
			title = "Ветеринарная клиника для Хомяков",
			version = "0.1",
			description = "Spring Boot Backend for Hamster Clinic"
		)
	)
@SpringBootApplication
public class HamsterClinicApplication {

	public static void main(String[] args) {
		SpringApplication.run(HamsterClinicApplication.class, args);
	}

}
