package com.upgradechallenge.volcanocamp.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.upgradechallenge.volcanocamp.exception.BadRequestException;
import com.upgradechallenge.volcanocamp.exception.MethodNotAllowedException;
import com.upgradechallenge.volcanocamp.exception.OccupiedPeriodException;
import com.upgradechallenge.volcanocamp.exception.ResourceNotFoundException;
import com.upgradechallenge.volcanocamp.model.Reservation;
import com.upgradechallenge.volcanocamp.repository.ReservationRepository;

@Service
public class ReservationService {

	private static final String VALIDATION_ERROR_DATE_QUERY_PARAMS = "The check-in date must be before the check-out date";
	private static final String VALIDATION_ERROR_ID = "The reservation id must be valid";
	private static final String VALIDATION_ERROR_ACTIVE_STATUS = "The reservation that has been cancelled cannot be updated";

	@Autowired
	ReservationRepository reservationRepo;

	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	public List<LocalDate> getAllAvailableDates(LocalDate startDate, LocalDate endDate) {

		List<LocalDate> availableDates = new ArrayList<>();
		List<LocalDate> occupiedDates = new ArrayList<>();

		if (startDate.isAfter(endDate)) {
			throw new BadRequestException(VALIDATION_ERROR_DATE_QUERY_PARAMS);
		}

		List<Reservation> activeReservationsInPeriod = reservationRepo.findActiveReservationsInInterval(startDate,
				endDate);

		if (activeReservationsInPeriod.isEmpty()) {
			availableDates = extractDatesBetweenTwoDates(startDate, endDate);
		} else {
			// Get all occupied dates
			for (Reservation res : activeReservationsInPeriod) {
				List<LocalDate> occupiedDatesForRes = extractDatesBetweenTwoDates(res.getCheckinDate(),
						res.getCheckoutDate());
				occupiedDates = Stream.of(occupiedDates, occupiedDatesForRes).flatMap(Collection::stream)
						.collect(Collectors.toList());
			}
			// Go through each date candidate and verify if the date is not in between
			// reservation periods
			for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
				if (!occupiedDates.contains(date)) {
					availableDates.add(date);
				}
			}
		}
		
		return availableDates;
	}
	
	/**
	 * 
	 * @param reservationId
	 * @return
	 */
	public Reservation getReservationById(String reservationId) {
		
		validateUUID(reservationId);
		UUID resUUID = UUID.fromString(reservationId);
		
		Optional<Reservation> resOptional = this.reservationRepo.findById(resUUID);

		if (resOptional.isEmpty()) {
			throw new ResourceNotFoundException(reservationId);
		}
		
		return resOptional.get();
	}

	/**
	 * 
	 * @param reservationToSave
	 * @return
	 */
	@Transactional
	public synchronized Reservation createNewReservation(Reservation reservationToSave) {
		// Check if the provided reservation overlaps with any existing reservations
		int datesToBookLength = extractDatesBetweenTwoDates(reservationToSave.getCheckinDate(),
				reservationToSave.getCheckoutDate()).size();
		int datesAvailableWithinBookingPeriod = getAllAvailableDates(reservationToSave.getCheckinDate(),
				reservationToSave.getCheckoutDate()).size();

		if (datesToBookLength != datesAvailableWithinBookingPeriod) {
			throw new OccupiedPeriodException();
		}
		reservationToSave.setActive(true);
		
		return reservationRepo.save(reservationToSave);
	}
	
	/**
	 * 
	 * @param reservationToUpdate
	 * @return
	 */
	@Transactional
	public synchronized Reservation updateReservation(String reservationId, Reservation reservationToUpdate) {

		validateUUID(reservationId);
		UUID resUUID = UUID.fromString(reservationId);
		Optional<Reservation> resOptional = this.reservationRepo.findById(resUUID);

		if (resOptional.isEmpty()) {
			throw new ResourceNotFoundException(reservationId);
		}

		Reservation savedReservation = resOptional.get();
		
		// Fast fail if reservation is cancelled
		if(!savedReservation.isActive()) {
			throw new MethodNotAllowedException(VALIDATION_ERROR_ACTIVE_STATUS);
		}
		// Fetch all reservation provided in the updated period in case period changed
		// and check if any exist, minus own reservation
		if (reservationToUpdate.getCheckinDate() != savedReservation.getCheckinDate()
				|| reservationToUpdate.getCheckoutDate() != savedReservation.getCheckoutDate()) {
			List<Reservation> activeReservationsInPeriod = reservationRepo
					.findActiveReservationsInInterval(reservationToUpdate.getCheckinDate(),
							reservationToUpdate.getCheckoutDate())
					.stream().filter(r -> r.getId() != resUUID).collect(Collectors.toList());

			if(activeReservationsInPeriod.size() > 0) {
				throw new OccupiedPeriodException();
			}		
			savedReservation.setCheckinDate(reservationToUpdate.getCheckinDate());
			savedReservation.setCheckoutDate(reservationToUpdate.getCheckoutDate());
		}
		
		savedReservation.setUserFullName(reservationToUpdate.getUserFullName());
		savedReservation.setUserEmail(reservationToUpdate.getUserEmail());
		
		return reservationRepo.save(savedReservation);
	}

	/**
	 * 
	 * @param reservationId
	 * @return
	 */
	public Reservation cancelReservation(String reservationId) {
		
		validateUUID(reservationId);
		UUID resUUID = UUID.fromString(reservationId);
		
		Optional<Reservation> resOptional = this.reservationRepo.findById(resUUID);

		if (resOptional.isEmpty()) {
			throw new ResourceNotFoundException(reservationId);
		}

		Reservation savedReservation = resOptional.get();
		savedReservation.setActive(false);
		savedReservation.setCancelledDate(LocalDate.now());

		return reservationRepo.save(savedReservation);
	}

	private List<LocalDate> extractDatesBetweenTwoDates(LocalDate startDate, LocalDate endDate) {
		return Stream.iterate(startDate, date -> date.plusDays(1)).limit(ChronoUnit.DAYS.between(startDate, endDate))
				.collect(Collectors.toList());
	}
	
	private void validateUUID(String uuid) {
		try {
			UUID.fromString(uuid);
		}catch(Exception e) {
			throw new BadRequestException(VALIDATION_ERROR_ID);
		}
	}
}