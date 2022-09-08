package com.catenax.dft.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;

public class DftException extends RuntimeException {

	public DftException(String message) {
		super(message);
	}

	public DftException(JsonProcessingException message) {
		super(message);
	}

}
