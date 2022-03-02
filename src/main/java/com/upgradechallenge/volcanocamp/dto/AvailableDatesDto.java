package com.upgradechallenge.volcanocamp.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailableDatesDto {

	private LocalDate fromDate;
	private LocalDate toDate;
	private List<LocalDate> availableDates;

}