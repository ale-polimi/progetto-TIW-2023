package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.dao.ProductDAO;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.IDException;
import it.polimi.tiw.progetto2023.utils.ServletErrorResponse;

@WebServlet("/LoadVisualized")
@MultipartConfig
public class LoadVisualized extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public LoadVisualized() {
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
		ProductDAO productDao = new ProductDAO(connection);
		List<Product> products = new ArrayList<Product>();
		List<Integer> listaVisualizzati = new LinkedList<>();
		
		Gson gson = new Gson();
		
		try {
			// Loading JSON content
			String postContent = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			listaVisualizzati = Arrays.asList(gson.fromJson(postContent, Integer[].class));
			if(listaVisualizzati == null) {
				throw new Exception("Malformed request. listaVisualizzati is null.");
			}
		} catch (Exception e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		if(!listaVisualizzati.isEmpty()) {
			for(Integer id : listaVisualizzati) {
				try {
					products.add(productDao.getProductById(id));
				} catch (SQLException e) {
					ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to load visualised products.");
					return;
				} catch (IDException e) {
					ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
					return;
				}
			}
		}
		
		
		if(products.size() < 5) {
			// Fill with random products
			try {
				products.addAll(productDao.getProducts(listaVisualizzati, 5 - products.size()));
			} catch (SQLException e) {
				ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to load random products.");
				return;
			}
		}
		
		String jsonProducts = gson.toJson(products);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(jsonProducts);
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
