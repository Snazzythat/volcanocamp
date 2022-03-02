package com.upgradechallenge.volcanocamp.controller;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.upgradechallenge.volcanocamp.dto.AvailableDatesDto;
import com.upgradechallenge.volcanocamp.dto.ReservationDto;
import com.upgradechallenge.volcanocamp.model.Reservation;
import com.upgradechallenge.volcanocamp.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Reservations", description = "Operations pertaining to reservations and available dates.")
@RestController
public class ReservationsController {

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ReservationsController.class);

	@Autowired
	ReservationService reservationService;

	@GetMapping(value = "/api/v1/available-dates", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Get available dates", description = "Get a list of all available dates for the campsite."
			+ "If boundaries are provided as parameters, the dates will be limited to those boundaries.")
	public ResponseEntity<AvailableDatesDto> getAllAvailableDates(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate) {

		LocalDate minAvailableDate = LocalDate.now().plusDays(1);
		LocalDate maxAvailableDate = LocalDate.now().plusMonths(1);

		log.info("Handle fetching all available reservation dates provided time period");

		// Default start to today + 1 day and end to today + 1 month if not provided
		// and if fromDate < minAvailableDate or toDate > maxAvailableDate
		LocalDate startDate = (fromDate == null || fromDate.isBefore(minAvailableDate)) ? minAvailableDate : fromDate;
		LocalDate endDate = (toDate == null || toDate.isAfter(maxAvailableDate)) ? maxAvailableDate : toDate;

		log.info("Start date provided (or adjusted to minimimum possible start date): {}", startDate);
		log.info("End date provided (or adjusted to maximum possible end date): {}", endDate);

		List<LocalDate> availableDates = this.reservationService.getAllAvailableDates(startDate, endDate);

		AvailableDatesDto datesDto = AvailableDatesDto.builder().fromDate(startDate).toDate(endDate)
				.availableDates(availableDates).build();

		log.info("Response: {}", datesDto);

		return ResponseEntity.ok(datesDto);
	}

	@PostMapping(value = "/api/v1/reservations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Create a new Reservation", description = "Reserve a stay at the camp by submitting a Reservation with valid booking dates, email and full name"
			+ "If boundaries are provided as parameters, the dates will be limited to those boundaries.")
	public ResponseEntity<ReservationDto> createNewReservation(@RequestBody @Valid ReservationDto reservationDto) {

		log.info("Handle submitting a new reservation");

		Reservation reservation = convertDtoToModel(reservationDto);
		reservation = this.reservationService.createNewReservation(reservation);

		log.info("Response: {}", reservation);

		return new ResponseEntity<ReservationDto>(convertModelToDto(reservation), HttpStatus.CREATED);
	}

	@GetMapping(value = "/api/v1/reservations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Fetch a Reservation", description = "Fetch a Reservation by providing a valid UUID")
	public ResponseEntity<ReservationDto> getReservation(@PathVariable(required = true) String id) {

		log.info("Handle fetching of a reservation provided the id: {}", id);

		Reservation reservation = this.reservationService.getReservationById(id);

		log.info("Response: {}", reservation);

		return ResponseEntity.ok(convertModelToDto(reservation));
	}

	@PatchMapping(value = "/api/v1/reservations/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Update a reservation", description = "Update a Reservation by providing a valid UUID as well as the updated Reservation fields.")
	public ResponseEntity<ReservationDto> updateReservation(@PathVariable(required = true) String id,
			@RequestBody @Valid ReservationDto reservationDto) {

		log.info("Handle updating a reservation provided the id: {}", id);

		Reservation reservation = convertDtoToModel(reservationDto);
		reservation = this.reservationService.updateReservation(id, reservation);

		log.info("Response: {}", reservation);

		return new ResponseEntity<ReservationDto>(convertModelToDto(reservation), HttpStatus.OK);
	}

	@DeleteMapping(value = "/api/v1/reservations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Delete a reservation", description = "Delete a Reservation by providing a valid UUID."
			+ " A Reservation is returned back with isActive field set to false indicating that the Reservation was indeed cancelled.")
	public ResponseEntity<ReservationDto> cancelReservation(@PathVariable(required = true) String id) {

		log.info("Handle cancelling of a reservation provided the id: {}", id);

		Reservation reservation = this.reservationService.cancelReservation(id);

		log.info("Response: {}", reservation);

		return new ResponseEntity<ReservationDto>(convertModelToDto(reservation), HttpStatus.OK);
	}

	private Reservation convertDtoToModel(ReservationDto reservationDto) {
		return Reservation.builder().userEmail(reservationDto.getUserEmail())
				.userFullName(reservationDto.getUserFullName())
				.checkinDate(LocalDate.parse(reservationDto.getCheckinDate()))
				.checkoutDate(LocalDate.parse(reservationDto.getCheckoutDate())).build();
	}

	private ReservationDto convertModelToDto(Reservation reservation) {
		return ReservationDto.builder().userEmail(reservation.getUserEmail())
				.userFullName(reservation.getUserFullName()).checkinDate(reservation.getCheckinDate().toString())
				.checkoutDate(reservation.getCheckoutDate().toString()).id(reservation.getId())
				.isActive(reservation.isActive()).build();
	}

}
