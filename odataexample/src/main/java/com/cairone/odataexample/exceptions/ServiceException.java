package com.cairone.odataexample.exceptions;


public class ServiceException extends Exception {

	private static final long serialVersionUID = 1L;
	public static final int UNKNOWN = 0;
	public static final int ENTITY_NOT_FOUND = 1;
	public static final int DATA_INTEGRITY_VIOLATION = 2;
	public static final int MISSING_DATA = 3;
	public static final int TRANSACION_API_EXCEPTION = 4;
	
	private int code;
	
	public ServiceException(int code, String message) {
		super(message);
		this.code = code;
	}
	
	public ServiceException(String message) {
		this(UNKNOWN, message);
	}

	public int getCode() {
		return code;
	}
}
