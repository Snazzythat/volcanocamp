package com.upgradechallenge.volcanocamp.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.upgradechallenge.volcanocamp.model.ReservationDate;

@Repository
public interface ReservationDateRepository extends JpaRepository<ReservationDate, UUID> {
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select rd from ReservationDate rd where rd.date >= ?1 and rd.date < ?2")
	List<ReservationDate> findActiveReservationsInInterval(LocalDate fromDate, LocalDate toDate);
	
	@Query("select rd from ReservationDate rd where rd.date >= ?1 and rd.date < ?2")
	List<ReservationDate> findActiveReservationsInIntervalNonLocked(LocalDate fromDate, LocalDate toDate);
	
}
