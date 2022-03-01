package com.upgradechallenge.volcanocamp.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Order(Ordered.HIGHEST_PRECEDENCE) 
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BadRequestException.class)
	protected ResponseEntity<OperationError> handleInvalidPeriodException(BadRequestException ex) {
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Bad request error", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(OccupiedPeriodException.class)
	protected ResponseEntity<OperationError> handleOccupiedPeriodException(OccupiedPeriodException ex) {
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Occupied period error", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	protected ResponseEntity<OperationError> handleResourceNotFoundException(ResourceNotFoundException ex) {
		OperationError error = new OperationError(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<OperationError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		List<String> details = new ArrayList<>();
		for (ObjectError error : ex.getBindingResult().getAllErrors()) {
			details.add(error.getDefaultMessage());
		}
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Validation error", details);
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<OperationError> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		String detailMessage = "Missing body or bad payload field format. Please see API doc for proper request field format. Details: ";
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Data format error", detailMessage + ex.getCause().getLocalizedMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<OperationError> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Query parameter format error", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConversionFailedException.class)
	protected ResponseEntity<OperationError> handleConversionFailedException(ConversionFailedException ex) {
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Bad query parameter format error",
				ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodNotAllowedException.class)
	protected ResponseEntity<OperationError> handleMethodNotAllowedException(MethodNotAllowedException ex) {
		OperationError error = new OperationError(HttpStatus.METHOD_NOT_ALLOWED, "Operation is not allowed",
				ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	protected ResponseEntity<OperationError> handleNoHandlerFoundException(NoHandlerFoundException ex) {
		OperationError error = new OperationError(HttpStatus.NOT_FOUND, "Handler not found", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(MissingPathVariableException.class)
	public ResponseEntity<Object> handleMissingPathVariableException(MissingPathVariableException ex) {
		OperationError error = new OperationError(HttpStatus.BAD_REQUEST, "Missing path parameter", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleDefaultException(Exception ex) {
		OperationError error = new OperationError(HttpStatus.INTERNAL_SERVER_ERROR, "Server-side error",
				ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
