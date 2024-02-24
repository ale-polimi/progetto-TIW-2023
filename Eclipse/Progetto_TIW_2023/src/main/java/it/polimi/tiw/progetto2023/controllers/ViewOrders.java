package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.progetto2023.beans.Order;
import it.polimi.tiw.progetto2023.beans.User;
import it.polimi.tiw.progetto2023.dao.OrderDAO;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.IDException;

@WebServlet("/ViewOrders")
public class ViewOrders extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public ViewOrders() {
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
		List<Order> ordersToShow = new ArrayList<Order>();
		OrderDAO orderDao = new OrderDAO(connection);
		HttpSession session = req.getSession();
		
		try {
			ordersToShow = orderDao.getOrdersOfUserById(((User)session.getAttribute("user")).getId());
		} catch (SQLException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch user's orders.");
			e.printStackTrace();
		} catch (IDException e) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		String path = "/WEB-INF/orders.html";
		ServletContext servletContext = getServletContext();
		final WebContext webContext = new WebContext(req, resp, servletContext, req.getLocale());
		webContext.setVariable("orders", ordersToShow);
		templateEngine.process(path, webContext, resp.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
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
