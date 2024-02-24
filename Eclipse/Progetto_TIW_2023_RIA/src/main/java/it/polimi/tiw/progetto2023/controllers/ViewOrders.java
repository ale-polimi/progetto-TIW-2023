package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.progetto2023.beans.Order;
import it.polimi.tiw.progetto2023.beans.User;
import it.polimi.tiw.progetto2023.dao.OrderDAO;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.IDException;
import it.polimi.tiw.progetto2023.utils.ServletErrorResponse;

@WebServlet("/ViewOrders")
@MultipartConfig
public class ViewOrders extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public ViewOrders() {
		super();
	}
	
	@Override
	public void init() throws ServletException {
		connection = ConnectionManager.getConnection(getServletContext());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		List<Order> ordersToShow = new ArrayList<Order>();
		OrderDAO orderDao = new OrderDAO(connection);
		HttpSession session = req.getSession();
		
		try {
			ordersToShow = orderDao.getOrdersOfUserById(((User)session.getAttribute("user")).getId());
		} catch (SQLException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch user's orders.");
			return;
		} catch (IDException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		Gson gson = new Gson();
		String jsonOrders = gson.toJson(ordersToShow);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(jsonOrders);
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
