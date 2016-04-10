package com.github.jextractall.exceptions;

public class UnknownOperationResultException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UnknownOperationResultException() {
		super("Unknown extract operation result");
	}

}
