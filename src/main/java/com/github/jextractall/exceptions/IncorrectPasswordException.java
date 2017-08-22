package com.github.jextractall.exceptions;

public class IncorrectPasswordException extends RuntimeException {

	private static final long serialVersionUID = 1L;
    private String password;

	public IncorrectPasswordException(String password) {
        super("Extraction failed: incorrect password");
        this.password = password;
    }
	
	public String getUsedPassword() {
	    return password;
	}
}
