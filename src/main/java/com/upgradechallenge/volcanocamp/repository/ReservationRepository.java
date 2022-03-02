package com.upgradechallenge.volcanocamp.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.upgradechallenge.volcanocamp.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
	Optional<Reservation> findById(UUID id);
}
