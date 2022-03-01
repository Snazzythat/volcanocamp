package com.upgradechallenge.volcanocamp.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.UUID;

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

import com.upgradechallenge.volcanocamp.dto.ReservationDto;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ReservationsControllerIntegrationTest {

	private static final String TEST_USER_FULLNAME = "Test user";
	private static final String TEST_USER_EMAIL = "test@mail.com";
	private static final LocalDate CHECKIN_DATE = LocalDate.now().plusDays(1);
	private static final LocalDate CHECKOUT_DATE = LocalDate.now().plusDays(2);

	@Autowired
	private TestRestTemplate restTemplate;

	private RestTemplate patchRestTemplate;

	@LocalServerPort
	private int randomServerPort;

	@Before
	public void setup() {
		// Below is necessary to get PATCH working with RestTemplate
		patchRestTemplate = restTemplate.getRestTemplate();
		HttpClient httpClient = HttpClientBuilder.create().build();
		patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
	}

	@Test
	public void testSuccessfulReservationCreationFetchThenUpdateAndCancellation() throws URISyntaxException {
		final String baseUrl = "http://localhost:" + randomServerPort + "/api/v1/reservations";
		URI url = new URI(baseUrl);

		ReservationDto reservation = ReservationDto.builder().checkinDate(CHECKIN_DATE.toString())
				.checkoutDate(CHECKOUT_DATE.toString()).userEmail(TEST_USER_EMAIL).userFullName(TEST_USER_FULLNAME)
				.build();

		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "application/json");

		HttpEntity<ReservationDto> request = new HttpEntity<>(reservation, headers);

		// Test creation of a new Reservation
		ResponseEntity<ReservationDto> result = this.restTemplate.postForEntity(url, request, ReservationDto.class);

		assertEquals(201, result.getStatusCodeValue());

		UUID savedReservationId = result.getBody().getId();
		assertNotNull(savedReservationId);

		// Test fetch of the created Reservation, check when active
		result = this.restTemplate.getForEntity(baseUrl + '/' + savedReservationId, ReservationDto.class);
		assertEquals(200, result.getStatusCodeValue());
		assertTrue(result.getBody().isActive());

		// Test update of name, email and reservation dates
		ReservationDto reservationToUpdate = ReservationDto.builder().checkinDate(CHECKIN_DATE.plusDays(2).toString())
				.checkoutDate(CHECKOUT_DATE.plusDays(3).toString()).userEmail("changed_" + TEST_USER_EMAIL)
				.userFullName(TEST_USER_FULLNAME + "_changed").build();

		request = new HttpEntity<>(reservationToUpdate, headers);

		result = this.patchRestTemplate.exchange(baseUrl + '/' + savedReservationId, HttpMethod.PATCH, request,
				ReservationDto.class);

		assertEquals(200, result.getStatusCodeValue());
        assertEquals(CHECKIN_DATE.plusDays(2).toString(), result.getBody().getCheckinDate());
        assertEquals(CHECKOUT_DATE.plusDays(3).toString(), result.getBody().getCheckoutDate());
        assertTrue(result.getBody().isActive());

	}
}
