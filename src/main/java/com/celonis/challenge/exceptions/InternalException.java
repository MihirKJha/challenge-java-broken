package com.celonis.challenge.exceptions;

/**
 * InternalException
 * 
 * @author User
 *
 */
public class InternalException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
    public InternalException(Exception e) {
        super(e);
    }

    public InternalException(String message) {
        super(message);
    }

}
