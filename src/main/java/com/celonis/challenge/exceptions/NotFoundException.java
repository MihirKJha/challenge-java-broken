package com.celonis.challenge.exceptions;

/**
 * NotFoundException
 */
public class NotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
		super(message);
	}
	
	public NotFoundException() {
		super();
	}
}
