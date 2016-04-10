package com.github.jextractall.exceptions;

public class UnknownCompressionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnknownCompressionException() {
		super("Extraction failed: unknown compression method");
	}

}
