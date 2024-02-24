package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.progetto2023.dao.AddressDAO;
import it.polimi.tiw.progetto2023.dao.UserDAO;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;

@WebServlet("/Register")
public class Register extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public Register() {
		super();
	}

	@Override
	public void init() throws ServletException {
		connection = ConnectionManager.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = "/WEB-INF/register.html";
		resp.setContentType("text");
		ServletContext servletContext = getServletContext();
		final WebContext webContext = new WebContext(req, resp, servletContext, req.getLocale());
		templateEngine.process(path, webContext, resp.getWriter());
	}

	@SuppressWarnings("null")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String name = req.getParameter("name");
		String surname = req.getParameter("surname");
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		String city = req.getParameter("city");
		String road = req.getParameter("road");
		String zipCode = req.getParameter("zipCode");
		int number = 0;
		boolean invalidNumber = false;
		
		try {
			number = Integer.parseInt(req.getParameter("number"));
			if(number < 1) {
				invalidNumber = true;
			}
		} catch (NumberFormatException e) {
			invalidNumber = true;
		}
		
		String path;
		if(name == null || surname == null || email == null || password == null || city == null || road == null || zipCode == null ||
		   name.isBlank() || surname.isBlank() || email.isBlank() || password.isBlank() || city.isBlank() || road.isBlank() || zipCode.isBlank() || invalidNumber) {
			// Invalid or empty fields.
			ServletContext servletContext = getServletContext();
			final WebContext webContext = new WebContext(req, resp, servletContext, req.getLocale());
			webContext.setVariable("nameReceived", (name != null || !name.isBlank() ? name : ""));
			webContext.setVariable("surnameReceived", (surname != null || !surname.isBlank() ? surname : ""));
			webContext.setVariable("emailReceived", (email != null || !email.isBlank() ? email : ""));
			webContext.setVariable("cityReceived", (city != null || !city.isBlank() ? city : ""));
			webContext.setVariable("roadReceived", (road != null || !road.isBlank() ? road : ""));
			webContext.setVariable("zipCodeReceived", (zipCode != null || !zipCode.isBlank() ? zipCode : ""));
			webContext.setVariable("numberReceived", (!invalidNumber ? String.valueOf(number) : ""));
			webContext.setVariable("errorMsg", "All the fields must be filled in.");
			path = "/WEB-INF/register.html";
			templateEngine.process(path, webContext, resp.getWriter());
		} else {
			UserDAO userDao = new UserDAO(connection);
			AddressDAO addressDao = new AddressDAO(connection);
			int addressId = -1;
			
			try {
				addressId = addressDao.getAddressIdByParameters(city, road, zipCode, number);
			} catch (SQLException e){
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to add address to the database.");
				return;
			}
			
			try {
				userDao.addUser(name, surname, email, password, addressId);
			} catch (SQLException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to add user to the database.");
				return;
			}
			
			path = getServletContext().getContextPath() + "/login.html";
			resp.sendRedirect(path);
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
