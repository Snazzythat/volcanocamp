package com.upgradechallenge.volcanocamp.dto;

import java.util.UUID;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.upgradechallenge.volcanocamp.utils.ValidReservationDates;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidReservationDates
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema
public class ReservationDto {
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private UUID id;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private boolean active;
	
	@Schema(description = "User full name", example="John Doe")
	@NotBlank(message = "Name must not be emty")
	private String userFullName;

	@Schema(description = "User email address", example="john.doe@upgrade.com")
	@NotBlank(message = "Email must not be empty")
	@Email(message = "Email must be a valid e-mail")
	private String userEmail;

	@Schema(description = "Check-in date", format= "yyyy-MM-dd", example="2022-03-20")
	private String checkinDate;

	@Schema(description = "Check-out date", format= "yyyy-MM-dd",example="2022-03-22")
	private String checkoutDate;
	
}
