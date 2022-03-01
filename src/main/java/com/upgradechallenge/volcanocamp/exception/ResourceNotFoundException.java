package com.upgradechallenge.volcanocamp.exception;

public class ResourceNotFoundException  extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private static final String NOT_FOUND_ERROR= "Reservation with id %s is not found";
	
	public ResourceNotFoundException(String resourceId) {
        super(String.format(NOT_FOUND_ERROR, resourceId));
    }
}