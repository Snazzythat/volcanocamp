package com.upgradechallenge.volcanocamp.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.upgradechallenge.volcanocamp.configuration.ReservationConfiguration;
import com.upgradechallenge.volcanocamp.dto.ReservationDto;

public class ReservationDatesValidator implements ConstraintValidator<ValidReservationDates, ReservationDto> {
	
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(ReservationDatesValidator.class);

	private static final String VALIDATION_ERROR_DATES_REQUIRED = "Both check-in and check-out dates must be provided.";
	private static final String VALIDATION_ERROR_DATES_FUTURE = "Both check-in and check-out dates must be in the future.";
	private static final String VALIDATION_ERROR_CHECKIN_AFTER_CHECKOUT = "The check-in date must be before the check-out date";
	private static final String VALIDATION_ERROR_RESERVATION_LENGTH = "The reservation at the campside must be between 1 and 3 days";
	private static final String VALIDATION_ERROR_RESERVATION_START_LIMITS = "The reservation at the campside can be placed minimum 1 "
			+ " day ahead of arrival and up to 1 month in advance";
	private static final String VALIDATION_ERROR_INVALID_DATE_FORMAT = "Both check-in and check-out dates must have valid format: yyyy-MM-dd";

	@Autowired
	ReservationConfiguration reservationConfig;

	@Override
	public void initialize(ValidReservationDates constraintAnnotation) {

	}

	/**
	 * Validates Reservation request dates syntactically and schematically given a Reservation request.
	 * 
	 * @param requestReservation (ReservationDto) Reservation request
	 * @param constraintContext (ConstraintValidatorContext) Constraint context
	 * @return true if both check-in and check-out dates are valid
	 */
	@Override
	public boolean isValid(ReservationDto requestReservation, ConstraintValidatorContext constraintContext) {
		
		LocalDate requestCheckinDate = null;
		LocalDate requestCheckoutDate = null;
		try {
			String checkinDate = requestReservation.getCheckinDate();
			String checkinOutDate = requestReservation.getCheckoutDate();
			if (checkinDate != null && checkinOutDate != null) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				requestCheckinDate = LocalDate.parse(checkinDate, formatter);
				requestCheckoutDate = LocalDate.parse(checkinOutDate, formatter);
			}
		} catch (Exception e) {
			log.debug("Validate field format: failed");
			this.setConstraintViolationInContext(VALIDATION_ERROR_INVALID_DATE_FORMAT, constraintContext);
			return false;
		}
		log.debug("Validate field format: passed");

		// Check if both dates are provided
		// This can also be done via @NotNull in the DTO field validation
		if (requestCheckinDate == null || requestCheckoutDate == null) {
			log.debug("Both dates provided and not null: failed");
			this.setConstraintViolationInContext(VALIDATION_ERROR_DATES_REQUIRED, constraintContext);
			return false;
		}
		log.debug("Both dates provided and not null: passed");

		// Check if both dates are in the future
		LocalDate today = LocalDate.now();
		if (today.isAfter(requestCheckinDate) || (today.isAfter(requestCheckoutDate))) {
			log.debug("Both dates provided are in the future: failed");
			this.setConstraintViolationInContext(VALIDATION_ERROR_DATES_FUTURE, constraintContext);
			return false;
		}
		log.debug("Both dates provided are in the future: passed");

		// Check if check-in date comes before check-out
		if (requestCheckinDate.isAfter(requestCheckoutDate)) {
			log.debug("Check-in date is before check-out date: failed");
			this.setConstraintViolationInContext(VALIDATION_ERROR_CHECKIN_AFTER_CHECKOUT, constraintContext);
			return false;
		}
		log.debug("Check-in date is before check-out date: passed");

		// Check for reservation length
		long reservationLength = ChronoUnit.DAYS.between(requestCheckinDate, requestCheckoutDate);

		if (reservationLength < reservationConfig.getMinLength() || reservationLength > reservationConfig.getMaxLength()) {
			log.debug("Reservation length constraint: failed");
			this.setConstraintViolationInContext(VALIDATION_ERROR_RESERVATION_LENGTH, constraintContext);
			return false;
		}
		log.debug("Reservation length constraint: passed");

		// Check for reservation start limits
		LocalDate minReservationStart = today.plusDays(reservationConfig.getMinStartOffsetDays());
		LocalDate maxReservationStart = today.plusDays(reservationConfig.getMaxStartOffsetDays());

		if (requestCheckinDate.isBefore(minReservationStart) || requestCheckinDate.isAfter(maxReservationStart)) {
			log.debug("Reservation check-in min and max dates: failed");
			this.setConstraintViolationInContext(VALIDATION_ERROR_RESERVATION_START_LIMITS, constraintContext);
			return false;
		}	
		log.debug("Reservation check-in min and max dates: passed");
		
		return true;
	}

	private void setConstraintViolationInContext(String violationMessage,
			ConstraintValidatorContext constraintContext) {
		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
	}

}
