package com.upgradechallenge.volcanocamp.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AvailableDatesDto {

	private LocalDate fromDate;
	private LocalDate toDate;
	private List<LocalDate> availableDates;

}