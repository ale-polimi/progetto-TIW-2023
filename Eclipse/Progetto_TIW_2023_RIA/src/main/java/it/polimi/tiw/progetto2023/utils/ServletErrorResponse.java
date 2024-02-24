package it.polimi.tiw.progetto2023.utils;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class ServletErrorResponse {

	/**
	 * Method to send an error to the client.
	 * @param response is the response.
	 * @param code is the error HTTP error code.
	 * @param message is the error message.
	 * @throws IOException if the response can't be written.
	 */
	public static void createResponse(HttpServletResponse response, int code, String message) throws IOException {
		response.setStatus(code);
		response.setContentType("text/html");
		response.getWriter().println(message);
		return;
	}

}
