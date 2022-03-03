package com.upgradechallenge.volcanocamp.utils;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = ReservationDatesValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidReservationDates {
	String message() default "Reservation dates are not valid";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}