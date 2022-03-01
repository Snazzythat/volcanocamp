package com.upgradechallenge.volcanocamp.dto;

import java.util.UUID;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.upgradechallenge.volcanocamp.utils.ValidReservationDates;

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
public class ReservationDto {
	
	private UUID id;

	@NotBlank(message = "Name must not be emty")
	private String userFullName;

	@NotBlank(message = "Email must not be empty")
	@Email(message = "Email must be a valid e-mail")
	private String userEmail;

	private String checkinDate;

	private String checkoutDate;
	
	private boolean isActive;
	
}
