package com.upgradechallenge.volcanocamp.model;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reservations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = { "id" })
public class Reservation {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	@Column(name = "user_name")
	private String userFullName;

	@Column(name = "user_email")
	private String userEmail;

	@Column(name = "checkin_date")
	private LocalDate checkinDate;

	@Column(name = "checkout_date")
	private LocalDate checkoutDate;

	@Column(name = "cancelled_date")
	private LocalDate cancelledDate;

	@Column(name = "is_active")
	private boolean active;

}
