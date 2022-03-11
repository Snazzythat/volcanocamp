package com.upgradechallenge.volcanocamp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.upgradechallenge.volcanocamp.exception.BadRequestException;
import com.upgradechallenge.volcanocamp.exception.MethodNotAllowedException;
import com.upgradechallenge.volcanocamp.exception.OccupiedPeriodException;
import com.upgradechallenge.volcanocamp.exception.ResourceNotFoundException;
import com.upgradechallenge.volcanocamp.model.Reservation;
import com.upgradechallenge.volcanocamp.model.ReservationDate;
import com.upgradechallenge.volcanocamp.repository.ReservationDateRepository;
import com.upgradechallenge.volcanocamp.repository.ReservationRepository;

@RunWith(MockitoJUnitRunner.class)
public class ReservationServiceTest {

	private static final String MOCK_UUID = "c1614525-f582-4702-b886-db95d4489a4a";

	@Mock
	ReservationRepository reservationRepoMock;

	@Mock
	ReservationDateRepository reservationDateRepoMock;

	@InjectMocks
	ReservationService reservationService = new ReservationService();

	@Test
	public void givenTwoValidDatesAndNoReservations_getAllAvailableDates_shouldReturnAvailableDatesBetweenProvidedDates() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);

		when(reservationDateRepoMock.findActiveReservationsInIntervalNonLocked(startDate, endDate))
				.thenReturn(new ArrayList<>());

		List<LocalDate> availableDates = reservationService.getAllAvailableDates(startDate, endDate);

		assertTrue(availableDates.size() == 3);
	}

	@Test
	public void givenValidReservation_getAllAvailableDates_shouldReturnOnlyOneAvailableDate() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);

		List<ReservationDate> mockResDates = getReservationDateListFromRange(startDate, endDate);

		when(reservationDateRepoMock.findActiveReservationsInIntervalNonLocked(startDate, endDate)).thenReturn(mockResDates);

		List<LocalDate> availableDates = reservationService.getAllAvailableDates(startDate, endDate);

		assertTrue(availableDates.size() == 1);
	}

	@Test
	public void givenTwoValidReservationOfTwoDays_getAllAvailableDates_shouldReturnAvailableDatesForReservationPeriodExcludingTheReservedDates() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(10);

		LocalDate startDateRes1 = LocalDate.now().plusDays(2);
		LocalDate endDateRes1 = LocalDate.now().plusDays(4);
		List<ReservationDate> mockResDates = getReservationDateListFromRange(startDateRes1, endDateRes1);

		LocalDate startDateRes2 = LocalDate.now().plusDays(6);
		LocalDate endDateRes2 = LocalDate.now().plusDays(8);
		mockResDates.addAll(getReservationDateListFromRange(startDateRes2, endDateRes2));

		when(reservationDateRepoMock.findActiveReservationsInIntervalNonLocked(startDate, endDate))
				.thenReturn(mockResDates);

		List<LocalDate> availableDates = reservationService.getAllAvailableDates(startDate, endDate);

		assertTrue(availableDates.size() == 6);
	}

	@Test(expected = BadRequestException.class)
	public void givenCheckinDateAfterCheckoutDate_getAllAvailableDates_shouldThrowException() {
		LocalDate startDate = LocalDate.now().plusDays(10);
		LocalDate endDate = LocalDate.now().plusDays(5);

		reservationService.getAllAvailableDates(startDate, endDate);
	}

	@Test
	public void givenValidReservationId_getReservationById_shouldReturnTheAssociatedReservation() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(10);
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional
				.of(Reservation.builder().checkinDate(startDate).checkoutDate(endDate).id(uuid).build());
		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);

		Reservation reservation = reservationService.getReservationById(MOCK_UUID);

		assertEquals(mockResOptional.get(), reservation);
	}

	@Test(expected = BadRequestException.class)
	public void givenInvalidUUID_getReservationById_shouldThrowException() {
		reservationService.getReservationById("abc1234");
	}

	@Test(expected = ResourceNotFoundException.class)
	public void givenReservationId_andReservationDoesNotExist_getReservationById_shouldThrowException() {
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional.empty();
		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);

		reservationService.getReservationById(MOCK_UUID);
	}

	@Test
	public void givenValidReservationAndNoOccupiedDatesInRange_createNewReservation_shouldCreateAndReturnNewActiveReservation() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(10);

		Reservation reservationToSave = Reservation.builder().checkinDate(startDate).checkoutDate(endDate).build();
		Reservation expectedReservation = Reservation.builder().checkinDate(startDate).checkoutDate(endDate)
				.id(UUID.fromString(MOCK_UUID)).active(true).build();
		
		when(reservationDateRepoMock.findActiveReservationsInInterval(startDate, endDate)).thenReturn(new ArrayList<>());
		when(reservationRepoMock.save(reservationToSave)).thenReturn(expectedReservation);

		Reservation savedReservation = reservationService.createNewReservation(reservationToSave);

		assertEquals(expectedReservation, savedReservation);
		assertTrue(savedReservation.isActive());
	}

	@Test(expected = OccupiedPeriodException.class)
	public void givenValidReservationWithOverlappingReservationInRange_createNewReservation_shouldThrowException() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);

		LocalDate startDateOverlapping = LocalDate.now().plusDays(2);
		LocalDate endDateOverlapping = LocalDate.now().plusDays(4);

		Reservation reservationToSave = Reservation.builder().checkinDate(startDate).checkoutDate(endDate).build();
		List<ReservationDate> mockResDates = getReservationDateListFromRange(startDateOverlapping, endDateOverlapping);
		
		when(reservationDateRepoMock.findActiveReservationsInInterval(startDate, endDate))
		.thenReturn(mockResDates);

		reservationService.createNewReservation(reservationToSave);
	}

	@Test
	public void givenValidReservationWithNewNonOverlappingDates_updateReservation_shouldUpdateAndReturnNewReservation() {

		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional
				.of(Reservation.builder().checkinDate(startDate).checkoutDate(endDate).id(uuid).active(true).build());

		LocalDate startDateUpdate = LocalDate.now().plusDays(2);
		LocalDate endDateUpdate = LocalDate.now().plusDays(4);

		Reservation reservationToSave = Reservation.builder().checkinDate(startDateUpdate).checkoutDate(endDateUpdate)
				.id(UUID.fromString(MOCK_UUID)).active(true).build();

		Reservation expectedUpdatedReservation = Reservation.builder().checkinDate(startDateUpdate)
				.checkoutDate(endDateUpdate).id(UUID.fromString(MOCK_UUID)).active(true).build();

		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);
		when(reservationRepoMock.save(reservationToSave)).thenReturn(expectedUpdatedReservation);

		Reservation savedReservation = reservationService.updateReservation(MOCK_UUID, reservationToSave);

		assertEquals(startDateUpdate, savedReservation.getCheckinDate());
		assertEquals(endDateUpdate, savedReservation.getCheckoutDate());
	}

	@Test(expected = OccupiedPeriodException.class)
	public void givenValidReservationWithOverlappingDates_updateReservation_shouldThrowException() {

		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional
				.of(Reservation.builder().checkinDate(startDate).checkoutDate(endDate).id(uuid).active(true).build());

		LocalDate startDateUpdate = LocalDate.now().plusDays(3);
		LocalDate endDateUpdate = LocalDate.now().plusDays(5);

		Reservation reservationToSave = Reservation.builder().checkinDate(startDateUpdate).checkoutDate(endDateUpdate)
				.id(UUID.fromString(MOCK_UUID)).active(true).build();

		LocalDate startDateOverlapping = LocalDate.now().plusDays(4);
		LocalDate endDateOverlapping = LocalDate.now().plusDays(6);

		List<ReservationDate> mockResDates = getReservationDateListFromRange(startDateOverlapping, endDateOverlapping);

		when(reservationDateRepoMock.findActiveReservationsInInterval(startDateUpdate, endDateUpdate)).thenReturn(mockResDates);
		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);

		reservationService.updateReservation(MOCK_UUID, reservationToSave);
	}

	@Test(expected = MethodNotAllowedException.class)
	public void givenIdOfCancelledReservation_updateReservation_shouldThrowException() {

		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional.of(
				Reservation.builder().checkinDate(startDate).checkoutDate(endDate).id(uuid).active(false).build());

		LocalDate startDateUpdate = LocalDate.now().plusDays(3);
		LocalDate endDateUpdate = LocalDate.now().plusDays(5);

		Reservation reservationToSave = Reservation.builder().checkinDate(startDateUpdate).checkoutDate(endDateUpdate)
				.id(UUID.fromString(MOCK_UUID)).build();

		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);

		reservationService.updateReservation(MOCK_UUID, reservationToSave);
	}

	@Test(expected = BadRequestException.class)
	public void givenInvalidUUID_updateReservation_shouldThrowException() {
		Reservation reservationToSave = Reservation.builder().checkinDate(LocalDate.now().plusDays(1))
				.checkoutDate(LocalDate.now().plusDays(3)).id(UUID.fromString(MOCK_UUID)).build();

		reservationService.updateReservation("abc1234", reservationToSave);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void givenReservationId_andReservationDoesNotExist_updateReservation_shouldThrowException() {
		Reservation reservationToSave = Reservation.builder().checkinDate(LocalDate.now().plusDays(1))
				.checkoutDate(LocalDate.now().plusDays(3)).id(UUID.fromString(MOCK_UUID)).build();
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional.empty();
		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);

		reservationService.updateReservation(MOCK_UUID, reservationToSave);
	}
	
	@Test
	public void givenValidReservationId_cancelReservation_shouldCancelAndReturnTheAssociatedReservationAsInactive() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(10);
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional
				.of(Reservation.builder().checkinDate(startDate).checkoutDate(endDate).id(uuid).active(true).build());
		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);
		
		Reservation expectedUpdatedReservation = Reservation.builder().checkinDate(startDate)
				.checkoutDate(endDate).id(UUID.fromString(MOCK_UUID)).active(false).build();
		when(reservationRepoMock.save(mockResOptional.get())).thenReturn(expectedUpdatedReservation);
		
		Reservation reservation = reservationService.cancelReservation(MOCK_UUID);

		assertFalse(reservation.isActive());
	}

	@Test(expected = BadRequestException.class)
	public void givenInvalidUUID_cancelReservation_shouldThrowException() {
		reservationService.cancelReservation("abc1234");
	}

	@Test(expected = ResourceNotFoundException.class)
	public void givenReservationId_andReservationDoesNotExist_cancelReservation_shouldThrowException() {
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional.empty();
		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);

		reservationService.cancelReservation(MOCK_UUID);
	}

	private List<ReservationDate> getReservationDateListFromRange(LocalDate startDate, LocalDate endDate) {
		List<LocalDate> dates = Stream.iterate(startDate, date -> date.plusDays(1))
				.limit(ChronoUnit.DAYS.between(startDate, endDate)).collect(Collectors.toList());
		return dates.stream().map(localDate -> ReservationDate.builder().date(localDate).build())
				.collect(Collectors.toList());
	}
}
