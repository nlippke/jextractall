package com.github.jextractall.exceptions;

public class DataErrorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DataErrorException() {
		super("Extraction failed: data error");
	}

}
