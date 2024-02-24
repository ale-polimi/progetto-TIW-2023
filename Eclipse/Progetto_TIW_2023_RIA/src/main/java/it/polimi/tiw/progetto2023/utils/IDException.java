package it.polimi.tiw.progetto2023.utils;

public class IDException extends Exception {

	private static final long serialVersionUID = 1L;
	private String exceptionMessage = "The specified ID does not exist.";
	
	@Override
	public String getMessage() {
		return this.exceptionMessage;
	}
}
