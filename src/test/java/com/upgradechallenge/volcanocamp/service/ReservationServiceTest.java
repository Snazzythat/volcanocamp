package com.upgradechallenge.volcanocamp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
import com.upgradechallenge.volcanocamp.repository.ReservationRepository;

@RunWith(MockitoJUnitRunner.class)
public class ReservationServiceTest {

	private static final String MOCK_UUID = "c1614525-f582-4702-b886-db95d4489a4a";

	@Mock
	ReservationRepository reservationRepoMock;

	@InjectMocks
	ReservationService reservationService = new ReservationService();

	@Test
	public void givenTwoValidDatesAndNoReservations_getAllAvailableDates_shouldReturnAvailableDatesBetweenProvidedDates() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(10);

		when(reservationRepoMock.findActiveReservationsInInterval(startDate, endDate)).thenReturn(new ArrayList<>());

		List<LocalDate> availableDates = reservationService.getAllAvailableDates(startDate, endDate);

		assertTrue(availableDates.size() == 9);
	}

	@Test
	public void givenValidReservation_getAllAvailableDates_shouldReturnNoAvailableDatesForReservationPeriod() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(10);

		List<Reservation> mockResList = Arrays
				.asList(Reservation.builder().checkinDate(startDate).checkoutDate(endDate).build());

		when(reservationRepoMock.findActiveReservationsInInterval(startDate, endDate)).thenReturn(mockResList);

		List<LocalDate> availableDates = reservationService.getAllAvailableDates(startDate, endDate);

		assertTrue(availableDates.size() == 0);
	}

	@Test
	public void givenTwoValidReservationOfTwoDays_getAllAvailableDates_shouldReturnAvailableDatesForReservationPeriodExcludingTheReservedDates() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(10);

		LocalDate startDateRes1 = LocalDate.now().plusDays(2);
		LocalDate endDateRes1 = LocalDate.now().plusDays(4);

		LocalDate startDateRes2 = LocalDate.now().plusDays(6);
		LocalDate endDateRes2 = LocalDate.now().plusDays(8);

		Reservation mockRes1 = Reservation.builder().checkinDate(startDateRes1).checkoutDate(endDateRes1).build();
		Reservation mockRes2 = Reservation.builder().checkinDate(startDateRes2).checkoutDate(endDateRes2).build();

		when(reservationRepoMock.findActiveReservationsInInterval(startDate, endDate))
				.thenReturn(Arrays.asList(mockRes1, mockRes2));

		List<LocalDate> availableDates = reservationService.getAllAvailableDates(startDate, endDate);

		assertTrue(availableDates.size() == 5);
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
				.id(UUID.fromString(MOCK_UUID)).isActive(true).build();

		when(reservationRepoMock.findActiveReservationsInInterval(startDate, endDate)).thenReturn(new ArrayList<>());
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
		Reservation reservationOverlapping = Reservation.builder().checkinDate(startDateOverlapping)
				.checkoutDate(endDateOverlapping).build();

		when(reservationRepoMock.findActiveReservationsInInterval(startDate, endDate))
				.thenReturn(Arrays.asList(reservationOverlapping));

		reservationService.createNewReservation(reservationToSave);
	}

	@Test
	public void givenValidReservationWithNewNonOverlappingDates_updateReservation_shouldUpdateAndReturnNewReservation() {

		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional
				.of(Reservation.builder().checkinDate(startDate).checkoutDate(endDate).id(uuid).isActive(true).build());

		LocalDate startDateUpdate = LocalDate.now().plusDays(2);
		LocalDate endDateUpdate = LocalDate.now().plusDays(4);

		Reservation reservationToSave = Reservation.builder().checkinDate(startDateUpdate).checkoutDate(endDateUpdate)
				.id(UUID.fromString(MOCK_UUID)).isActive(true).build();

		Reservation expectedUpdatedReservation = Reservation.builder().checkinDate(startDateUpdate)
				.checkoutDate(endDateUpdate).id(UUID.fromString(MOCK_UUID)).isActive(true).build();

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
				.of(Reservation.builder().checkinDate(startDate).checkoutDate(endDate).id(uuid).isActive(true).build());

		LocalDate startDateUpdate = LocalDate.now().plusDays(3);
		LocalDate endDateUpdate = LocalDate.now().plusDays(5);

		Reservation reservationToSave = Reservation.builder().checkinDate(startDateUpdate).checkoutDate(endDateUpdate)
				.id(UUID.fromString(MOCK_UUID)).isActive(true).build();

		LocalDate startDateOverlapping = LocalDate.now().plusDays(4);
		LocalDate endDateOverlapping = LocalDate.now().plusDays(6);

		Reservation reservationOverlapping = Reservation.builder().checkinDate(startDateOverlapping)
				.checkoutDate(endDateOverlapping).isActive(true).id(UUID.fromString(MOCK_UUID.stripTrailing())).build();

		when(reservationRepoMock.findActiveReservationsInInterval(startDateUpdate, endDateUpdate))
				.thenReturn(Arrays.asList(reservationOverlapping));
		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);

		reservationService.updateReservation(MOCK_UUID, reservationToSave);
	}

	@Test(expected = MethodNotAllowedException.class)
	public void givenIdOfCancelledReservation_updateReservation_shouldThrowException() {

		LocalDate startDate = LocalDate.now().plusDays(1);
		LocalDate endDate = LocalDate.now().plusDays(3);
		UUID uuid = UUID.fromString(MOCK_UUID);

		Optional<Reservation> mockResOptional = Optional.of(
				Reservation.builder().checkinDate(startDate).checkoutDate(endDate).id(uuid).isActive(false).build());

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
				.of(Reservation.builder().checkinDate(startDate).checkoutDate(endDate).id(uuid).isActive(true).build());
		when(reservationRepoMock.findById(uuid)).thenReturn(mockResOptional);
		
		Reservation expectedUpdatedReservation = Reservation.builder().checkinDate(startDate)
				.checkoutDate(endDate).id(UUID.fromString(MOCK_UUID)).isActive(false).build();
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
}
