package com.upgradechallenge.volcanocamp.exception;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Order(Ordered.HIGHEST_PRECEDENCE) 
@ControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BadRequestException.class)
	protected ResponseEntity<OperationError> handleInvalidPeriodException(BadRequestException ex) {
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Bad request error", ex.getMessage());
		log.error("Error occured: {}",ex.getMessage());
		log.debug("Exception details: {}",ex);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(OccupiedPeriodException.class)
	protected ResponseEntity<OperationError> handleOccupiedPeriodException(OccupiedPeriodException ex) {
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Occupied period error", ex.getMessage());
		log.error("Error occured: {}",ex.getMessage());
		log.debug("Exception details: {}",ex);
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	protected ResponseEntity<OperationError> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Occupied period error", ex.getMessage());
		log.error("Error occured: {}",ex.getMessage());
		log.debug("Exception details: {}",ex);
		return new ResponseEntity<>(error, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	protected ResponseEntity<OperationError> handleResourceNotFoundException(ResourceNotFoundException ex) {
		OperationError error = new OperationError(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
		log.error("Error occured: {}",ex.getMessage());
		log.debug("Exception details: {}",ex);
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<OperationError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		List<String> details = new ArrayList<>();
		for (ObjectError error : ex.getBindingResult().getAllErrors()) {
			details.add(error.getDefaultMessage());
		}
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Validation error", details);
		log.error("Error occured: {}",ex.getMessage());
		log.debug("Exception details: {}",ex);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<OperationError> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		String detailMessage = "Missing body or bad payload field format. Please see API doc for proper request field format. Details: ";
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Data format error", detailMessage + ex.getCause().getLocalizedMessage());
		log.error("Error occured: {}",ex.getMessage());
		log.debug("Exception details: {}",ex);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<OperationError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Query parameter format error", ex.getMessage());
		log.error("Error occured: {}",ex.getMessage());
		log.debug("Exception details: {}",ex);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodNotAllowedException.class)
	protected ResponseEntity<OperationError> handleMethodNotAllowedException(MethodNotAllowedException ex) {
		OperationError error = new OperationError(HttpStatus.METHOD_NOT_ALLOWED, "Operation is not allowed",
				ex.getMessage());
		log.error("Error occured: {}",ex.getMessage());
		log.debug("Exception details: {}",ex);
		return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	protected ResponseEntity<OperationError> handleNoHandlerFoundException(NoHandlerFoundException ex) {
		return buildErrorResponseEntity(HttpStatus.NOT_FOUND, "Handler not found for the provided API path", ex);
	}

	@ExceptionHandler(MissingPathVariableException.class)
	public ResponseEntity<OperationError> handleMissingPathVariableException(MissingPathVariableException ex) {
		return buildErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Missing path parameter", ex);
	}
	
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<OperationError> handleHttpRequestMethodNotSupportedException(Exception ex) {
		return buildErrorResponseEntity(HttpStatus.METHOD_NOT_ALLOWED, "This method is not supported for the provided API path", ex);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<OperationError> handleDefaultException(Exception ex) {
		return buildErrorResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Server-side error", ex);
	}
	
	private ResponseEntity<OperationError> buildErrorResponseEntity(HttpStatus status, String errorMessage, Exception ex) {
		OperationError error = new OperationError(status, errorMessage,
				ex.getMessage());
		log.error("Error occured: {}",ex.getMessage());
		log.debug("Exception details: {}",ex);
		return new ResponseEntity<OperationError>(error, status);
	}
}
