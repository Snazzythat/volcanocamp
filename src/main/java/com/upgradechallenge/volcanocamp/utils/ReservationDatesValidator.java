package com.upgradechallenge.volcanocamp.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.upgradechallenge.volcanocamp.dto.ReservationDto;

public class ReservationDatesValidator implements ConstraintValidator<ValidReservationDates, ReservationDto> {

	private static final String VALIDATION_ERROR_DATES_REQUIRED = "Both check-in and check-out dates must be provided.";
	private static final String VALIDATION_ERROR_DATES_FUTURE = "Both check-in and check-out dates must be in the future.";
	private static final String VALIDATION_ERROR_CHECKIN_AFTER_CHECKOUT = "The check-in date must be before the check-out date";
	private static final String VALIDATION_ERROR_RESERVATION_LENGTH = "The reservation at the campside must be between 1 and 3 days";
	private static final String VALIDATION_ERROR_RESERVATION_START_LIMITS = "The reservation at the campside can be placed minimum 1 "
			+ " day ahead of arrival and up to 1 month in advance";
	private static final String VALIDATION_ERROR_INVALID_DATE_FORMAT = "Both check-in and check-out dates must have valid format: yyyy-MM-dd";

	private static final long MIN_RESERVATION_LENGTH = 1;
	private static final long MAX_RESERVATION_LENGTH = 3;

	@Override
	public void initialize(ValidReservationDates constraintAnnotation) {

	}

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
			this.setConstraintViolationInContext(VALIDATION_ERROR_INVALID_DATE_FORMAT, constraintContext);
			return false;
		}

		// Check if both dates are provided
		// This can also be done via @NotNull in the DTO field validation
		if (requestCheckinDate == null || requestCheckoutDate == null) {
			this.setConstraintViolationInContext(VALIDATION_ERROR_DATES_REQUIRED, constraintContext);
			return false;
		}

		// Check if both dates are in the future
		LocalDate today = LocalDate.now();
		if (today.isAfter(requestCheckinDate) || (today.isAfter(requestCheckoutDate))) {
			this.setConstraintViolationInContext(VALIDATION_ERROR_DATES_FUTURE, constraintContext);
			return false;
		}

		// Check if check-in date comes before check-out
		if (requestCheckinDate.isAfter(requestCheckoutDate)) {
			this.setConstraintViolationInContext(VALIDATION_ERROR_CHECKIN_AFTER_CHECKOUT, constraintContext);
			return false;
		}

		// Check for reservation length
		long reservationLength = ChronoUnit.DAYS.between(requestCheckinDate, requestCheckoutDate);

		if (reservationLength < MIN_RESERVATION_LENGTH || reservationLength > MAX_RESERVATION_LENGTH) {
			this.setConstraintViolationInContext(VALIDATION_ERROR_RESERVATION_LENGTH, constraintContext);
			return false;
		}

		// Check for reservation start limits
		LocalDate minReservationStart = today.plusDays(1);
		LocalDate maxReservationStart = today.plusMonths(1);

		if (requestCheckinDate.isBefore(minReservationStart) || requestCheckinDate.isAfter(maxReservationStart)) {
			this.setConstraintViolationInContext(VALIDATION_ERROR_RESERVATION_START_LIMITS, constraintContext);
			return false;
		}

		return true;
	}

	private void setConstraintViolationInContext(String violationMessage,
			ConstraintValidatorContext constraintContext) {
		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(violationMessage).addConstraintViolation();
	}

}
