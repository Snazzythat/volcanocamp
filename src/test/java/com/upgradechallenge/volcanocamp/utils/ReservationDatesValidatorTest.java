package com.upgradechallenge.volcanocamp.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.upgradechallenge.volcanocamp.configuration.ReservationConfiguration;
import com.upgradechallenge.volcanocamp.dto.ReservationDto;
import com.upgradechallenge.volcanocamp.service.ReservationService;

@RunWith(MockitoJUnitRunner.class)
public class ReservationDatesValidatorTest {

	private static final String TEST_USER_FULLNAME = "Test user";
	private static final String TEST_USER_EMAIL = "test@mail.com";
	private static final LocalDate CHECKIN_DATE = LocalDate.now().plusDays(1);
	private static final LocalDate CHECKOUT_DATE = LocalDate.now().plusDays(2);

	@Mock
	ValidReservationDates validDates;

	@Mock
	ConstraintValidatorContext constraintValidatorContext;
	
	@Mock
	ConstraintViolationBuilder builder;
	
	@Mock
	ReservationConfiguration reservationConfigMock;
	
	@InjectMocks
	ReservationDatesValidator reservationDatesValidator = new ReservationDatesValidator();

	@Before
	public void init() {

		when(constraintValidatorContext.buildConstraintViolationWithTemplate(Mockito.any()))
				.thenReturn(builder);
		
		when(reservationConfigMock.getMinLength()).thenReturn(1);
		when(reservationConfigMock.getMaxLength()).thenReturn(3);
		when(reservationConfigMock.getMinStartOffsetDays()).thenReturn(1);
		when(reservationConfigMock.getMaxStartOffsetDays()).thenReturn(31);
	}

	@Test
	public void givenValidDates_onValidation_shouldReturnValid() {

		ReservationDto testDto = ReservationDto.builder().userFullName(TEST_USER_FULLNAME).userEmail(TEST_USER_EMAIL)
				.checkinDate(CHECKIN_DATE.toString()).checkoutDate(CHECKOUT_DATE.toString()).build();

		assertTrue(reservationDatesValidator.isValid(testDto, constraintValidatorContext));
	}
	
	@Test
	public void givenAnyReservationDateIsMissing_onValidation_shouldReturnInvalid() {

		ReservationDto testDto = ReservationDto.builder().userFullName(TEST_USER_FULLNAME).userEmail(TEST_USER_EMAIL).checkoutDate(CHECKOUT_DATE.toString()).build();

		assertFalse(reservationDatesValidator.isValid(testDto, constraintValidatorContext));
		
		testDto = ReservationDto.builder().userFullName(TEST_USER_FULLNAME).userEmail(TEST_USER_EMAIL).checkinDate(CHECKIN_DATE.toString()).build();
		
		assertFalse(reservationDatesValidator.isValid(testDto, constraintValidatorContext));
	}
	
	@Test
	public void givenDatesAreEqual_onValidation_shouldReturnInvalid() {

		ReservationDto testDto = ReservationDto.builder().userFullName(TEST_USER_FULLNAME).userEmail(TEST_USER_EMAIL)
				.checkinDate(CHECKIN_DATE.toString()).checkoutDate(CHECKIN_DATE.toString()).build();

		assertFalse(reservationDatesValidator.isValid(testDto, constraintValidatorContext));
	}
	
	@Test
	public void givenCheckinDateIsLessThanOneDayAway_onValidation_shouldReturnInvalid() {

		ReservationDto testDto = ReservationDto.builder().userFullName(TEST_USER_FULLNAME).userEmail(TEST_USER_EMAIL)
				.checkinDate(LocalDate.now().toString()).checkoutDate(CHECKOUT_DATE.toString()).build();

		assertFalse(reservationDatesValidator.isValid(testDto, constraintValidatorContext));
	}
	
	@Test
	public void givenCheckinDateIsGreaterThanOneMonthAway_onValidation_shouldReturnInvalid() {

		ReservationDto testDto = ReservationDto.builder().userFullName(TEST_USER_FULLNAME).userEmail(TEST_USER_EMAIL)
				.checkinDate(CHECKIN_DATE.plusMonths(1).toString()).checkoutDate(CHECKOUT_DATE.plusMonths(1).toString()).build();

		assertFalse(reservationDatesValidator.isValid(testDto, constraintValidatorContext));
	}
	
	@Test
	public void givenStayIsGreaterThanThreeDays_onValidation_shouldReturnInvalid() {

		ReservationDto testDto = ReservationDto.builder().userFullName(TEST_USER_FULLNAME).userEmail(TEST_USER_EMAIL)
				.checkinDate(CHECKIN_DATE.toString()).checkoutDate(CHECKOUT_DATE.plusDays(3).toString()).build();

		assertFalse(reservationDatesValidator.isValid(testDto, constraintValidatorContext));
	}
	
	@Test
	public void givenDatesAreInThePast_onValidation_shouldReturnInvalid() {

		ReservationDto testDto = ReservationDto.builder().userFullName(TEST_USER_FULLNAME).userEmail(TEST_USER_EMAIL)
				.checkinDate(LocalDate.now().minusDays(3).toString()).checkoutDate(LocalDate.now().minusDays(2).toString()).build();

		assertFalse(reservationDatesValidator.isValid(testDto, constraintValidatorContext));
	}
	
	@Test
	public void givenCheckinIsAfterCheckout_onValidation_shouldReturnInvalid() {

		ReservationDto testDto = ReservationDto.builder().userFullName(TEST_USER_FULLNAME).userEmail(TEST_USER_EMAIL)
				.checkinDate(CHECKOUT_DATE.toString()).checkoutDate(CHECKIN_DATE.toString()).build();

		assertFalse(reservationDatesValidator.isValid(testDto, constraintValidatorContext));
	}


}