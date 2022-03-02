package com.upgradechallenge.volcanocamp.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.upgradechallenge.volcanocamp.exception.BadRequestException;
import com.upgradechallenge.volcanocamp.exception.MethodNotAllowedException;
import com.upgradechallenge.volcanocamp.exception.OccupiedPeriodException;
import com.upgradechallenge.volcanocamp.exception.ResourceNotFoundException;
import com.upgradechallenge.volcanocamp.model.Reservation;
import com.upgradechallenge.volcanocamp.model.ReservationDate;
import com.upgradechallenge.volcanocamp.repository.ReservationDateRepository;
import com.upgradechallenge.volcanocamp.repository.ReservationRepository;

@Service
public class ReservationService {

	private static final String VALIDATION_ERROR_DATE_QUERY_PARAMS = "The check-in date must be before the check-out date";
	private static final String VALIDATION_ERROR_ID = "The reservation id must be valid";
	private static final String VALIDATION_ERROR_ACTIVE_STATUS = "The reservation that has been cancelled cannot be updated";

	@Autowired
	ReservationRepository reservationRepo;

	@Autowired
	ReservationDateRepository reservationDateRepo;

	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	@Transactional(readOnly = true)
	public synchronized List<LocalDate> getAllAvailableDates(LocalDate startDate, LocalDate endDate) {

		if (startDate.isAfter(endDate)) {
			throw new BadRequestException(VALIDATION_ERROR_DATE_QUERY_PARAMS);
		}

		// Add offset to endDate to include it in availability list
		List<LocalDate> potentialAvailableDates = extractDatesBetweenTwoDates(startDate, endDate.plusDays(1));
		List<ReservationDate> activeReservationDatesInPeriod = reservationDateRepo
				.findActiveReservationsInInterval(startDate, endDate);

		if (activeReservationDatesInPeriod.size() == 0) {
			return potentialAvailableDates;
		} else {
			List<LocalDate> occupiedDates = activeReservationDatesInPeriod.stream().map(res -> res.getDate())
					.collect(Collectors.toList());
			potentialAvailableDates.removeAll(occupiedDates);
		}

		return potentialAvailableDates;
	}

	/**
	 * 
	 * @param reservationId
	 * @return
	 */
	@Transactional(readOnly = true)
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
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Reservation createNewReservation(Reservation reservationToSave) {
		// Check if the provided reservation overlaps with any existing reservations
		List<ReservationDate> activeReservationDatesInPeriod = reservationDateRepo.findActiveReservationsInInterval(
				reservationToSave.getCheckinDate(), reservationToSave.getCheckoutDate());

		// no overlaps, safe to save
		if (activeReservationDatesInPeriod.isEmpty()) {
			reservationToSave.setActive(true);
			List<ReservationDate> datesToReserve = convertLocalDateListToReservationList(extractDatesBetweenTwoDates(
					reservationToSave.getCheckinDate(), reservationToSave.getCheckoutDate()));

			// persist dates of the new valid reservation
		    reservationDateRepo.saveAll(datesToReserve);
		    
		} else {
			throw new OccupiedPeriodException();
		}
		return reservationRepo.save(reservationToSave);
	}

	/**
	 * 
	 * @param reservationToUpdate
	 * @return
	 */
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Reservation updateReservation(String reservationId, Reservation reservationToUpdate) {

		validateUUID(reservationId);
		UUID resUUID = UUID.fromString(reservationId);
		Optional<Reservation> resOptional = this.reservationRepo.findById(resUUID);

		if (resOptional.isEmpty()) {
			throw new ResourceNotFoundException(reservationId);
		}

		Reservation savedReservation = resOptional.get();

		// Fast fail if reservation is cancelled
		if (!savedReservation.isActive()) {
			throw new MethodNotAllowedException(VALIDATION_ERROR_ACTIVE_STATUS);
		}

		// Check if the new dates of the reservation do not overlap, take into account
		// existing dates if update
		// will include old dates as well
		List<ReservationDate> activeReservationDatesInPeriod = reservationDateRepo.findActiveReservationsInInterval(
				reservationToUpdate.getCheckinDate(), reservationToUpdate.getCheckoutDate());
		List<ReservationDate> oldReservationDates = convertLocalDateListToReservationList(
				extractDatesBetweenTwoDates(savedReservation.getCheckinDate(), savedReservation.getCheckoutDate()));
		activeReservationDatesInPeriod.removeAll(oldReservationDates);

		// no overlaps, safe to update
		if (activeReservationDatesInPeriod.isEmpty()) {

			// Remove current reservation dates from ReservationDate table
			reservationDateRepo.deleteAll(oldReservationDates);

			// Save new dates to ReservationDate table
			List<ReservationDate> newReservationDates = convertLocalDateListToReservationList(
					extractDatesBetweenTwoDates(reservationToUpdate.getCheckinDate(),
							reservationToUpdate.getCheckoutDate()));
			
		    reservationDateRepo.saveAll(newReservationDates);

		} else {
			throw new OccupiedPeriodException();
		}

		savedReservation.setCheckinDate(reservationToUpdate.getCheckinDate());
		savedReservation.setCheckoutDate(reservationToUpdate.getCheckoutDate());
		savedReservation.setUserFullName(reservationToUpdate.getUserFullName());
		savedReservation.setUserEmail(reservationToUpdate.getUserEmail());

		return reservationRepo.save(savedReservation);
	}

	/**
	 * 
	 * @param reservationId
	 * @return
	 */
	@Transactional
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

		// Remove active dates
		List<ReservationDate> reservationDatesToRemove = convertLocalDateListToReservationList(
				extractDatesBetweenTwoDates(savedReservation.getCheckinDate(), savedReservation.getCheckoutDate()));
		reservationDateRepo.deleteAll(reservationDatesToRemove);

		return reservationRepo.save(savedReservation);
	}

	private List<LocalDate> extractDatesBetweenTwoDates(LocalDate startDate, LocalDate endDate) {
		return Stream.iterate(startDate, date -> date.plusDays(1)).limit(ChronoUnit.DAYS.between(startDate, endDate))
				.collect(Collectors.toList());
	}

	private void validateUUID(String uuid) {
		try {
			UUID.fromString(uuid);
		} catch (Exception e) {
			throw new BadRequestException(VALIDATION_ERROR_ID);
		}
	}

	private List<ReservationDate> convertLocalDateListToReservationList(List<LocalDate> localDates) {
		return localDates.stream().map(localDate -> ReservationDate.builder().date(localDate).build())
				.collect(Collectors.toList());
	}
}