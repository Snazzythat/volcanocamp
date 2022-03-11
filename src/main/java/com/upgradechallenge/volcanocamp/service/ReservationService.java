package com.upgradechallenge.volcanocamp.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
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

	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ReservationService.class);

	private static final String VALIDATION_ERROR_DATE_QUERY_PARAMS = "The check-in date must be before the check-out date";
	private static final String VALIDATION_ERROR_ID = "The reservation id must be valid";
	private static final String VALIDATION_ERROR_ACTIVE_STATUS = "The reservation that has been cancelled cannot be updated";

	@Autowired
	ReservationRepository reservationRepo;

	@Autowired
	ReservationDateRepository reservationDateRepo;

	/**
	 * Queries the database for all available reservation dates and returns a list of dates (LocalDate) available to reserve.
	 * 
	 * @param startDate (LocalDate) Beginning date of the availability period
	 * @param endDate (LocalDate) Ending date of the availability period
	 * @return (List<LocalDate>) List of available dates to reserve
	 */
	@Transactional(readOnly = true)
	public List<LocalDate> getAllAvailableDates(LocalDate startDate, LocalDate endDate) {

		if (startDate.isAfter(endDate)) {
			throw new BadRequestException(VALIDATION_ERROR_DATE_QUERY_PARAMS);
		}

		// Add offset to endDate to include it in availability list
		List<LocalDate> potentialAvailableDates = extractDatesBetweenTwoDates(startDate, endDate.plusDays(1));
		List<ReservationDate> activeReservationDatesInPeriod = reservationDateRepo
				.findActiveReservationsInInterval(startDate, endDate);

		if (activeReservationDatesInPeriod.size() == 0) {
			log.debug("No overlapping dates found in period from {} to {}", startDate, endDate);
			return potentialAvailableDates;
		} else {
			List<LocalDate> occupiedDates = activeReservationDatesInPeriod.stream().map(res -> res.getDate())
					.collect(Collectors.toList());
			log.debug("Occupied dates overlapping dates found in period from {} to {} : {}", startDate, endDate,
					occupiedDates);
			potentialAvailableDates.removeAll(occupiedDates);
		}

		return potentialAvailableDates;
	}

	/**
	 * Queries the database for a specific Reservation using the provided reservation id.
	 * 
	 * @param reservationId (String) Reservation id in UUID format
	 * @return (Reservation) Reservation with the matching id
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
	 * Creates and persists a new Reservation.
	 * 
	 * @param reservationToSave (Reservation) Reservation to save in the database
	 * @return (Reservation) Reservation saved in the database
	 */
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public Reservation createNewReservation(Reservation reservationToSave) {
		// Check if the provided reservation overlaps with any existing reservations
		List<ReservationDate> activeReservationDatesInPeriod = reservationDateRepo.findActiveReservationsInInterval(
				reservationToSave.getCheckinDate(), reservationToSave.getCheckoutDate());

		// no overlaps, safe to save
		if (activeReservationDatesInPeriod.isEmpty()) {
			log.debug("No overlapping dates found in period from {} to {}", reservationToSave.getCheckinDate(), reservationToSave.getCheckoutDate());
			reservationToSave.setActive(true);
			List<ReservationDate> datesToReserve = convertLocalDateListToReservationList(extractDatesBetweenTwoDates(
					reservationToSave.getCheckinDate(), reservationToSave.getCheckoutDate()));

			log.debug("Saving new reservation dates, making them unavailable for others: {}", datesToReserve);
			// persist dates of the new valid reservation
			reservationDateRepo.saveAll(datesToReserve);

		} else {
			throw new OccupiedPeriodException();
		}
		return reservationRepo.save(reservationToSave);
	}

	/**
	 * Updates and persists a given Reservation.
	 * 
	 * @param reservationToUpdate (Reservation) Reservation to update in the database
	 * @return Reservation updated in the database
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
		// existing dates if update will include old dates as well
		List<ReservationDate> activeReservationDatesInPeriod = reservationDateRepo.findActiveReservationsInInterval(
				reservationToUpdate.getCheckinDate(), reservationToUpdate.getCheckoutDate());
		List<ReservationDate> oldReservationDates = convertLocalDateListToReservationList(
				extractDatesBetweenTwoDates(savedReservation.getCheckinDate(), savedReservation.getCheckoutDate()));
		activeReservationDatesInPeriod.removeAll(oldReservationDates);

		// no overlaps, safe to update
		if (activeReservationDatesInPeriod.isEmpty()) {
			log.debug("No overlapping dates found in period from {} to {}", reservationToUpdate.getCheckinDate(), reservationToUpdate.getCheckoutDate());
			// Remove current reservation dates from ReservationDate table
			reservationDateRepo.deleteAll(oldReservationDates);

			log.debug("Removing old reservation dates, making them available for others: {}", oldReservationDates);
			// Save new dates to ReservationDate table
			List<ReservationDate> newReservationDates = convertLocalDateListToReservationList(
					extractDatesBetweenTwoDates(reservationToUpdate.getCheckinDate(),
							reservationToUpdate.getCheckoutDate()));
			log.debug("Saving new reservation dates, making them unavailable for others: {}", newReservationDates);
			
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
	 * Cancels a given Reservation by updating it in the database with the active field set to false
	 * 
	 * @param reservationId (String) Reservation id in UUID format
	 * @return Reservation with active field set to false
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
		
		log.debug("Removing old reservation dates, making them available for others: {}", reservationDatesToRemove);

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