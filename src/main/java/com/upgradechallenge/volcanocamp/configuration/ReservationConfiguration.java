package com.upgradechallenge.volcanocamp.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "reservation")
public class ReservationConfiguration {
	
	private int minLength;
	private int maxLength;
	private int minStartOffsetDays;
	private int maxStartOffsetDays;
	
}