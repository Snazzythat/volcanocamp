package com.upgradechallenge.volcanocamp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.upgradechallenge.volcanocamp.configuration.ReservationConfiguration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
		info = @Info(title = "Volcano Camp Reservation REST API", 
		version = "1.0", 
		description = "Volcano camp site reservation API allowing to reserve the camp site for a given period of time", 
		contact = @Contact(url = "https://github.com/Snazzythat/volcanocamp", 
		name = "Roman Andoni", 
		email = "roman.andoni@gmail.com")))
@SpringBootApplication
@EnableConfigurationProperties({
	ReservationConfiguration.class,
})
public class VolcanocampApplication {

	public static void main(String[] args) {
		SpringApplication.run(VolcanocampApplication.class, args);
	}
}
