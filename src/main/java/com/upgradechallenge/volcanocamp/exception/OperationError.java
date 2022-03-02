package com.upgradechallenge.volcanocamp.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;

public class OperationError {

	private String timestamp;
	private HttpStatus status;
	private String errorMessage;
	private List<String> details;

	private OperationError() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
		timestamp = now.format(formatter);
		details = new ArrayList<>();
	}

	public OperationError(HttpStatus status) {
		this();
		this.status = status;
	}

	public OperationError(HttpStatus status, String errorMessage) {
		this();
		this.status = status;
		this.errorMessage = errorMessage;
	}

	public OperationError(HttpStatus status, String errorMessage, List<String> details) {
		this();
		this.status = status;
		this.errorMessage = errorMessage;
		this.details = details;
	}

	public OperationError(HttpStatus status, String errorMessage, String detail) {
		this();
		this.status = status;
		this.errorMessage = errorMessage;
		this.details = Arrays.asList(detail);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public String getTimeStamp() {
		return timestamp;
	}

	public List<String> getDetails() {
		return details;
	}
}