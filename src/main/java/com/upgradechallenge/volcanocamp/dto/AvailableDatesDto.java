package com.upgradechallenge.volcanocamp.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class AvailableDatesDto {

	@Schema(description = "Check-in date", format= "yyyy-MM-dd")
	private LocalDate fromDate;
	
	@Schema(description = "Check-out date", format= "yyyy-MM-dd")
	private LocalDate toDate;
	
	@Schema(description = "Available dates list", format= "yyyy-MM-dd")
	private List<LocalDate> availableDates;

}