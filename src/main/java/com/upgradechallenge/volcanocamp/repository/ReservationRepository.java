package com.upgradechallenge.volcanocamp.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.upgradechallenge.volcanocamp.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
	
	Optional<Reservation> findById(UUID id);

	@Query(value="SELECT * FROM reservations res WHERE :from_date < res.checkout_date AND :to_date > res.checkin_date AND res.is_active = true", nativeQuery = true)
	List<Reservation> findActiveReservationsInInterval(@Param("from_date") LocalDate fromDate,
			@Param("to_date") LocalDate toDate);
}
