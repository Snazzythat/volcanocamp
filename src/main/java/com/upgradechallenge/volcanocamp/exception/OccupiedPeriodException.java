package com.upgradechallenge.volcanocamp.exception;

public class OccupiedPeriodException  extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	private static final String OCCUPIED_DATE_ERROR= "There is at least one unavailable date in the provided time period";
	
	public OccupiedPeriodException() {
        super(OCCUPIED_DATE_ERROR);
    }
}
