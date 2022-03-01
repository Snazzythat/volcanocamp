package com.upgradechallenge.volcanocamp.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

public class OperationError {

	   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	   private LocalDateTime timestamp;
	   private HttpStatus status;
	   private String errorMessage;
	   private List<String> details;

	   private OperationError() {
	       timestamp = LocalDateTime.now();
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

	    public List<String> getDetails() {
	        return details;
	    }
	}