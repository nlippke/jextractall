package com.github.jextractall.exceptions;

public class CRCException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CRCException() {
		super("Extraction failed: CRC-check failed");
	}

}
