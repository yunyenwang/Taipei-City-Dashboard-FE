package com.cht.demo;

public class ServiceException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ServiceException(String message) {
		super(message);
	}
	
	public ServiceException(Throwable cause) {
		super(cause);
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public static final ServiceException of(String format, Object... args) {
		return new ServiceException(String.format(format, args));
	}
}
