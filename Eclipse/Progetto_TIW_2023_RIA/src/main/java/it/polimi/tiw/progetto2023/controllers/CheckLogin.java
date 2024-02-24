package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.google.gson.Gson;

import it.polimi.tiw.progetto2023.beans.User;
import it.polimi.tiw.progetto2023.dao.UserDAO;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.ServletErrorResponse;

@WebServlet("/CheckLogin")
@MultipartConfig
public class CheckLogin extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CheckLogin() {
		 super();
	}
	
	@Override
	public void init() throws ServletException {
		connection = ConnectionManager.getConnection(getServletContext());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		// TODO - Debug print
		System.out.println("Is request multipart? " + ServletFileUpload.isMultipartContent(req));
		System.out.println("req.getParameter(\"email\"): " + req.getParameter("email") + "\nreq.getParameter(\"password\"): " + req.getParameter("password"));
		
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		
		
		if(email == null || password == null || email.isBlank() || password.isBlank()) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Blank or null credentials.");
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		User user = null;
		try {
			user = userDao.checkCredentials(email, password);
		} catch (SQLException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached.");
			return;
		}
		
		if(user == null) { // Wrong credentials
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials, wrong email or password.");
		} else { // Correct credentials, add user to the session
			req.getSession().setAttribute("user", user);
			
			String jsonUser = new Gson().toJson(user);
			resp.setContentType("application/json");
			resp.setCharacterEncoding("UTF-8");
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().write(jsonUser);
		}
	}
	
	@Override
	public void destroy() {
		try {
			ConnectionManager.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
