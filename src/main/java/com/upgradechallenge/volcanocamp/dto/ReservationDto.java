package com.upgradechallenge.volcanocamp.dto;

import java.util.UUID;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

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
@Schema
public class ReservationDto {

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private UUID id;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private boolean active;

	@Schema(description = "User full name", example = "John Doe", required = true)
	@NotBlank(message = "Name must not be emty")
	private String userFullName;

	@Schema(description = "User email address", example = "john.doe@upgrade.com", required = true)
	@NotBlank(message = "Email must not be empty")
	@Email(message = "Email must be a valid e-mail")
	private String userEmail;

	@Schema(description = "Check-in date", format = "yyyy-MM-dd", example = "2022-03-20", required = true)
	@NotBlank(message = "Check-in date must be provided")
	private String checkinDate;

	@Schema(description = "Check-out date", format = "yyyy-MM-dd", example = "2022-03-22", required = true)
	@NotBlank(message = "Check-out date must be provided")
	private String checkoutDate;

}
