package com.upgradechallenge.volcanocamp.controller;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.upgradechallenge.volcanocamp.dto.AvailableDatesDto;
import com.upgradechallenge.volcanocamp.dto.ReservationDto;
import com.upgradechallenge.volcanocamp.model.Reservation;
import com.upgradechallenge.volcanocamp.service.ReservationService;

@RestController
@RequestMapping(value = "/api/v1/reservations")
public class ReservationsController {

	@Autowired
	ReservationService reservationService;

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AvailableDatesDto> getAllAvailableDates(
			@RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
			@RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate) {
		
		LocalDate minAvailableDate = LocalDate.now().plusDays(1);
		LocalDate maxAvailableDate = LocalDate.now().plusMonths(1);

		// Default start to today + 1 day and end to today + 1 month if not provided
		// and if fromDate < minAvailableDate or toDate > maxAvailableDate
		LocalDate startDate = (fromDate == null || fromDate.isBefore(minAvailableDate)) ? minAvailableDate : fromDate;
		LocalDate endDate = (toDate == null || toDate.isAfter(maxAvailableDate)) ? maxAvailableDate : toDate;

		List<LocalDate> availableDates = this.reservationService.getAllAvailableDates(startDate, endDate);

		AvailableDatesDto datesDto = AvailableDatesDto.builder().fromDate(startDate).toDate(endDate)
				.availableDates(availableDates).build();

		return ResponseEntity.ok(datesDto);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReservationDto> createNewReservation(@RequestBody @Valid ReservationDto reservationDto) {

		Reservation reservation = convertDtoToModel(reservationDto);
		reservation = this.reservationService.createNewReservation(reservation);
		
		return new ResponseEntity<ReservationDto>(convertModelToDto(reservation), HttpStatus.CREATED);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReservationDto> getReservation(@PathVariable(required = true) String id) {

		Reservation reservation = this.reservationService.getReservationById(id);

		return ResponseEntity.ok(convertModelToDto(reservation));
	}

	@PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReservationDto> updateReservation(@PathVariable(required = true) String id,
			@RequestBody @Valid ReservationDto reservationDto) {

		Reservation reservation = convertDtoToModel(reservationDto);
		reservation = this.reservationService.updateReservation(id, reservation);

		return new ResponseEntity<ReservationDto>(convertModelToDto(reservation), HttpStatus.OK);
	}

	@DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReservationDto> cancelReservation(@PathVariable(required = true) String id) {

		Reservation reservation = this.reservationService.cancelReservation(id);

		return new ResponseEntity<ReservationDto>(convertModelToDto(reservation), HttpStatus.OK);
	}

	private Reservation convertDtoToModel(ReservationDto reservationDto) {
		return Reservation.builder().userEmail(reservationDto.getUserEmail())
				.userFullName(reservationDto.getUserFullName()).checkinDate(LocalDate.parse(reservationDto.getCheckinDate()))
				.checkoutDate(LocalDate.parse(reservationDto.getCheckoutDate())).build();
	}

	private ReservationDto convertModelToDto(Reservation reservation) {
		return ReservationDto.builder().userEmail(reservation.getUserEmail())
				.userFullName(reservation.getUserFullName()).checkinDate(reservation.getCheckinDate().toString())
				.checkoutDate(reservation.getCheckoutDate().toString()).id(reservation.getId()).isActive(reservation.isActive())
				.build();
	}

}
