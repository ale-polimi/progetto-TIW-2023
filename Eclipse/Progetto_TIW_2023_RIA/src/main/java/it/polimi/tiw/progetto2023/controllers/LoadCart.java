package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.polimi.tiw.progetto2023.beans.Cart;
import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.dao.ProductDAO;
import it.polimi.tiw.progetto2023.dao.SupplierDAO;
import it.polimi.tiw.progetto2023.utils.CalcExpenses;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.IDException;
import it.polimi.tiw.progetto2023.utils.ServletErrorResponse;

@WebServlet("/LoadCart")
@MultipartConfig
public class LoadCart extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public LoadCart() {
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
		SupplierDAO supplierDao = new SupplierDAO(connection);
		List<Cart> cartsToShow = new ArrayList<Cart>();
		Gson gson = new Gson();
		
		try {
			String postContent = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			
			// TODO - Debug print
			System.out.println("In: " + this.getClass() + " postContent is: " + postContent);
			
			cartsToShow = Arrays.asList(gson.fromJson(postContent, Cart[].class));
			
			if(cartsToShow == null) {
				throw new Exception("Malformed request. carts is null.");
			}
			
			for(Cart cart : cartsToShow) {
				for(Product product : cart.getProducts()) {
					if(product.getQuantity() < 1 || product.getQuantity() > 999) {
						throw new Exception("Quantity is out of range.");
					}
				}
			}
		} catch (Exception e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		try {
			for(Cart cart : cartsToShow) {
				List<Product> supplierList = new ArrayList<Product>();
				
				cart.setSupplier(supplierDao.getSupplierById(cart.getSupplier().getId()));
				
				for(Product product : cart.getProducts()) {
					Product productToAdd = productDao.getProductByIdAndSupplier(product.getId(), cart.getSupplier().getId());
					productToAdd.setQuantity(product.getQuantity());
					supplierList.add(productToAdd);
				}
				cart.setProducts(supplierList);
				
				cart.setShippingCost(CalcExpenses.calcShippingPrice(cart.getProducts(), cart.getSupplier()));
				cart.setTotalCost(CalcExpenses.calcPrice(cart.getProducts()));
			}
		} catch (SQLException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to load supplier from cookie.");
			return;
		} catch (IDException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		String jsonCarts = gson.toJson(cartsToShow);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(jsonCarts);
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
