package com.upgradechallenge.volcanocamp.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.upgradechallenge.volcanocamp.dto.AvailableDatesDto;
import com.upgradechallenge.volcanocamp.dto.ReservationDto;
import com.upgradechallenge.volcanocamp.exception.OperationError;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ReservationsControllerIntegrationTest {

	private static final String TEST_USER_FULLNAME = "Test user";
	private static final String TEST_USER_EMAIL = "test@mail.com";
	private static final String EXPECTED_ERROR_DETAILS_BOUNDARY = "The reservation at the campside can be placed minimum 1  "
			+ "day ahead of arrival and up to 1 month in advance";
	private static final String EXPECTED_ERROR_DATES_CONSTRAINT = "The check-in date must be before the check-out date";
	private static final LocalDate CHECKIN_DATE = LocalDate.now().plusDays(1);
	private static final LocalDate CHECKOUT_DATE = LocalDate.now().plusDays(2);

	@Autowired
	private TestRestTemplate restTemplate;

	@LocalServerPort
	private int randomServerPort;

	private RestTemplate patchRestTemplate;
	
	private String baseUrl;

	@Before
	public void setup() {
		// Below is necessary to get PATCH working with RestTemplate
		patchRestTemplate = restTemplate.getRestTemplate();
		HttpClient httpClient = HttpClientBuilder.create().build();
		patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
		baseUrl = "http://localhost:" + randomServerPort;
	}

	@Test
	public void testInvalidReservationBoundariesAndDateConstraints() {
		String url = getBaseUrlReservations();

		// Test creation of a new Reservation with check-in less than 1 day from today
		ReservationDto reservation = getReservationWithDates(LocalDate.now(), CHECKOUT_DATE);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<ReservationDto> request = new HttpEntity<>(reservation, headers);

		ResponseEntity<OperationError> errorResult = this.restTemplate.postForEntity(url, request,
				OperationError.class);

		assertEquals(400, errorResult.getStatusCodeValue());
		assertEquals(EXPECTED_ERROR_DETAILS_BOUNDARY, errorResult.getBody().getDetails().get(0));

		// Test creation of a new Reservation with check-in greater than 1 month from
		// today
		reservation = getReservationWithDates(CHECKIN_DATE.plusDays(1).plusMonths(1),
				CHECKIN_DATE.plusDays(3).plusMonths(1));
		request = new HttpEntity<>(reservation, headers);

		errorResult = this.restTemplate.postForEntity(url, request, OperationError.class);

		assertEquals(400, errorResult.getStatusCodeValue());
		assertEquals(EXPECTED_ERROR_DETAILS_BOUNDARY, errorResult.getBody().getDetails().get(0));

		// Test creation of a new Reservation with check-in greater than check-out date
		reservation = getReservationWithDates(CHECKIN_DATE.plusDays(3), CHECKIN_DATE.plusDays(1));
		request = new HttpEntity<>(reservation, headers);

		errorResult = this.restTemplate.postForEntity(url, request, OperationError.class);

		assertEquals(400, errorResult.getStatusCodeValue());
		assertEquals(EXPECTED_ERROR_DATES_CONSTRAINT, errorResult.getBody().getDetails().get(0));
	}

	@Test
	public void testGetAllAvailableDates() {
		String datesUrl = getBaseUrlAvailableDates();
		String reservationsUrl = getBaseUrlReservations();
		
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		HttpEntity<AvailableDatesDto> request = new HttpEntity<>(headers);

		// Test with no boundaries provided should return the whole month available
		// (minus present day)
		ResponseEntity<AvailableDatesDto> availabilitiesResult = this.restTemplate.exchange(datesUrl, HttpMethod.GET,
				request, AvailableDatesDto.class);

		assertEquals(31, availabilitiesResult.getBody().getAvailableDates().size());

		// Test with both boundary limits provided
		availabilitiesResult = this.restTemplate.exchange(datesUrl + "?fromDate={fromDate}&&toDate={toDate}", HttpMethod.GET,
				request, AvailableDatesDto.class, CHECKIN_DATE.toString(), CHECKIN_DATE.plusDays(5).toString());

		assertEquals(6, availabilitiesResult.getBody().getAvailableDates().size());

		// Test with invalid boundaries provided
		ResponseEntity<OperationError> errorResult = this.restTemplate.exchange(
				datesUrl + "?fromDate={fromDate}&&toDate={toDate}", HttpMethod.GET, request, OperationError.class,
				CHECKIN_DATE.plusDays(5).toString(), CHECKIN_DATE.toString());

		assertEquals(400, errorResult.getStatusCodeValue());
		assertEquals(EXPECTED_ERROR_DATES_CONSTRAINT, errorResult.getBody().getDetails().get(0));

		// Create a registration and get all dates again, the available dates must
		// decrease by the amount of registered days
		ReservationDto reservation = getReservationWithDates(CHECKIN_DATE, CHECKOUT_DATE.plusDays(1));

		headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");
		HttpEntity<ReservationDto> requestReservation = new HttpEntity<>(reservation, headers);
		ResponseEntity<ReservationDto> resultReservation = this.restTemplate.postForEntity(reservationsUrl, requestReservation,
				ReservationDto.class);
		assertEquals(201, resultReservation.getStatusCodeValue());

		availabilitiesResult = this.restTemplate.exchange(datesUrl, HttpMethod.GET, request, AvailableDatesDto.class);

		assertEquals(29, availabilitiesResult.getBody().getAvailableDates().size());

		// Cleanup by canceling the registration
		this.restTemplate.delete(reservationsUrl + '/' + resultReservation.getBody().getId());

	}

	@Test
	public void testSuccessfulReservationCreationFetchThenUpdate() {
		String url = getBaseUrlReservations();

		ReservationDto reservation = getReservationWithDates(CHECKIN_DATE, CHECKOUT_DATE);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<ReservationDto> request = new HttpEntity<>(reservation, headers);

		// Test creation of a new Reservation
		ResponseEntity<ReservationDto> result = this.restTemplate.postForEntity(url, request, ReservationDto.class);

		assertEquals(201, result.getStatusCodeValue());

		UUID savedReservationId = result.getBody().getId();
		assertNotNull(savedReservationId);

		// Test fetch of the created Reservation, check when active
		result = this.restTemplate.getForEntity(url + '/' + savedReservationId, ReservationDto.class);
		assertEquals(200, result.getStatusCodeValue());
		assertTrue(result.getBody().isActive());

		// Test update of reservation dates
		ReservationDto reservationToUpdate = getReservationWithDates(CHECKIN_DATE.plusDays(2),
				CHECKOUT_DATE.plusDays(3));
		request = new HttpEntity<>(reservationToUpdate, headers);

		result = this.patchRestTemplate.exchange(url + '/' + savedReservationId, HttpMethod.PATCH, request,
				ReservationDto.class);

		assertEquals(200, result.getStatusCodeValue());
		assertEquals(CHECKIN_DATE.plusDays(2).toString(), result.getBody().getCheckinDate());
		assertEquals(CHECKOUT_DATE.plusDays(3).toString(), result.getBody().getCheckoutDate());
		assertTrue(result.getBody().isActive());

		// Cleanup by canceling the registration
		this.restTemplate.delete(url + '/' + savedReservationId);

	}

	@Test
	public void testCreateReservationWithOverlappingAndNonOverlappingReservations() {
		String url = getBaseUrlReservations();

		ReservationDto reservation1 = getReservationWithDates(CHECKIN_DATE, CHECKOUT_DATE);
		ReservationDto reservation2 = getReservationWithDates(CHECKIN_DATE.plusDays(4), CHECKOUT_DATE.plusDays(5));
		ReservationDto reservationBetween = getReservationWithDates(CHECKOUT_DATE, CHECKIN_DATE.plusDays(4));
		ReservationDto reservationOverlapping = getReservationWithDates(CHECKIN_DATE.plusDays(2),
				CHECKOUT_DATE.plusDays(4));

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<ReservationDto> request = new HttpEntity<>(reservation1, headers);

		// Test creation of a new Reservation between two existing reservations
		// (non-overlapping)
		ResponseEntity<ReservationDto> result = this.restTemplate.postForEntity(url, request, ReservationDto.class);

		assertEquals(201, result.getStatusCodeValue());
		String reservation1Id = result.getBody().getId().toString();

		request = new HttpEntity<>(reservation2, headers);
		result = this.restTemplate.postForEntity(url, request, ReservationDto.class);

		assertEquals(201, result.getStatusCodeValue());
		String reservation2Id = result.getBody().getId().toString();

		request = new HttpEntity<>(reservationBetween, headers);
		result = this.restTemplate.postForEntity(url, request, ReservationDto.class);

		assertEquals(201, result.getStatusCodeValue());
		String reservation3Id = result.getBody().getId().toString();

		// Test an overlapping reservation attempt
		request = new HttpEntity<>(reservationOverlapping, headers);
		ResponseEntity<OperationError> errorResult = this.restTemplate.postForEntity(url, request,
				OperationError.class);

		assertEquals(409, errorResult.getStatusCodeValue());
		assertEquals("There is at least one unavailable date in the provided time period",
				errorResult.getBody().getDetails().get(0));

		// Cleanup by canceling the registration
		this.restTemplate.delete(url + '/' + reservation1Id);
		this.restTemplate.delete(url + '/' + reservation2Id);
		this.restTemplate.delete(url + '/' + reservation3Id);
	}

	@Test
	public void testConcurrentRegistrationSameReservationFlow() throws InterruptedException, ExecutionException {
		String baseUrl = getBaseUrlReservations();

		ReservationDto reservation = getReservationWithDates(CHECKIN_DATE, CHECKOUT_DATE);

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		ExecutorService executorService = Executors.newCachedThreadPool();
		List<Future<ResponseEntity<ReservationDto>>> calls = new ArrayList<>();

		// Setup 30 threads to send same booking request in parallel. Only one must
		// succeed with 201 status
		for (int i = 0; i < 30; i++) {
			calls.add(executorService.submit(() -> restTemplate.postForEntity(baseUrl,
					new HttpEntity<>(reservation, headers), ReservationDto.class)));
		}

		List<ResponseEntity<ReservationDto>> responses = new ArrayList<ResponseEntity<ReservationDto>>();

		for (Future<ResponseEntity<ReservationDto>> call : calls) {
			responses.add(call.get());
		}

		assertEquals(30, responses.size());
		List<ResponseEntity<ReservationDto>> successCreateResponses = responses.stream()
				.filter(response -> response.getStatusCodeValue() == 201).collect(Collectors.toList());
		assertEquals(1, successCreateResponses.size());

		// Cleanup by canceling the successful registration
		this.restTemplate.delete(baseUrl + '/' + successCreateResponses.get(0).getBody().getId());
	}

	@Test
	public void testConcurrentRegistrationTwoReservationsOverlappingFlow()
			throws InterruptedException, ExecutionException {
		String baseUrl = getBaseUrlReservations();

		// Test creation of two overlapping reservations, only one must succeed with 201
		// status
		ReservationDto reservation1 = getReservationWithDates(CHECKIN_DATE, CHECKOUT_DATE.plusDays(2));
		ReservationDto reservation2 = getReservationWithDates(CHECKIN_DATE.plusDays(1), CHECKOUT_DATE.plusDays(1));

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		ExecutorService executorService = Executors.newCachedThreadPool();
		List<Future<ResponseEntity<ReservationDto>>> calls = new ArrayList<>();

		calls.add(executorService.submit(() -> restTemplate.postForEntity(baseUrl,
				new HttpEntity<>(reservation1, headers), ReservationDto.class)));
		calls.add(executorService.submit(() -> restTemplate.postForEntity(baseUrl,
				new HttpEntity<>(reservation2, headers), ReservationDto.class)));

		List<ResponseEntity<ReservationDto>> responses = new ArrayList<ResponseEntity<ReservationDto>>();

		for (Future<ResponseEntity<ReservationDto>> call : calls) {
			responses.add(call.get());
		}

		assertEquals(2, responses.size());
		List<ResponseEntity<ReservationDto>> successCreateResponses = responses.stream()
				.filter(response -> response.getStatusCodeValue() == 201).collect(Collectors.toList());
		assertEquals(1, successCreateResponses.size());

		// Cleanup by canceling the successful registration
		this.restTemplate.delete(baseUrl + '/' + successCreateResponses.get(0).getBody().getId());
	}

	private ReservationDto getReservationWithDates(LocalDate fromDate, LocalDate toDate) {
		return ReservationDto.builder().checkinDate(fromDate.toString()).checkoutDate(toDate.toString())
				.userEmail(TEST_USER_EMAIL).userFullName(TEST_USER_FULLNAME).build();
	}

	private String getBaseUrlReservations() {
		return baseUrl + "/api/v1/reservations";
	}
	
	private String getBaseUrlAvailableDates() {
		return baseUrl + "/api/v1/available-dates";
	}
}
