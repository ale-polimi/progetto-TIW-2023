package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import it.polimi.tiw.progetto2023.beans.Cart;
import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.beans.User;
import it.polimi.tiw.progetto2023.dao.AddressDAO;
import it.polimi.tiw.progetto2023.dao.OrderDAO;
import it.polimi.tiw.progetto2023.dao.ProductDAO;
import it.polimi.tiw.progetto2023.dao.SupplierDAO;
import it.polimi.tiw.progetto2023.utils.CalcExpenses;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.IDException;
import it.polimi.tiw.progetto2023.utils.ServletErrorResponse;

@WebServlet("/CreateOrder")
@MultipartConfig
public class CreateOrder extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CreateOrder() {
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
		OrderDAO orderDao = new OrderDAO(connection);
		ProductDAO productDao = new ProductDAO(connection);
		SupplierDAO supplierDao = new SupplierDAO(connection);
		AddressDAO addressDao = new AddressDAO(connection);
		HttpSession session = req.getSession();
		List<Product> userProducts = new ArrayList<Product>();
		List<Cart> carts;
		float total = -1;
		int supplierId;
		int addressId = -1;
		Cart cart;
		Gson gson = new Gson();
		
		try { // Input cleaning
			
			// TODO - Debug print
			System.out.println("Is request multipart? " + ServletFileUpload.isMultipartContent(req));
			System.out.println("req.getParameter(\"citta\"): " + req.getParameter("citta") + "\nreq.getParameter(\"via\"): " + req.getParameter("via") + "\nreq.getParameter(\"numero\"): " + req.getParameter("numero") + "\nreq.getParameter(\"cap\"): " + req.getParameter("cap") + "\nreq.getParameter(\"cookieCart\"): " + req.getParameter("cookieCart"));
			
			/*
			carts = Arrays.asList(gson.fromJson(postContent, Cart[].class));
			*/
			
			carts = Arrays.asList(gson.fromJson(req.getParameter("cookieCart"), Cart[].class));
			
			if(carts == null || carts.size() < 1) {
				throw new Exception("Malformed request. carts is null.");
			}
			
			// TODO - Debug print
			System.out.println("Checked carts. Size = " + carts.size());
			
			cart = carts.get(0);
			supplierId = cart.getSupplier().getId();
			
			if(!supplierDao.existsSupplier(supplierId)) {
				throw new IDException();	
			}
			
			// TODO - Debug print
			System.out.println("Checked supplierId. ID = " + supplierId);
			
			if(req.getParameter("citta") == null || req.getParameter("citta") == "" ||
					   req.getParameter("via") == null || req.getParameter("via") == "" ||
					   req.getParameter("cap") == null || req.getParameter("cap") == "" ||
					   req.getParameter("numero") == null || req.getParameter("numero") == "") {
				throw new Exception("Address fields can't be empty.");
			}
			
			// TODO - Debug print
			System.out.println("Checked address form. formContent = " + req.getParameter("citta") + " " + req.getParameter("via") + " " + req.getParameter("cap") + " " + req.getParameter("numero"));
			
			try {
				Integer.parseInt(req.getParameter("cap"));
				Integer.parseInt(req.getParameter("numero"));
			} catch (NumberFormatException e) {
				ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Malformed request. Zip code and number must be integers.");
				return;
			}
			
			for(Product product : cart.getProducts()) {
				if(product.getQuantity() < 1 || product.getQuantity() > 999) {
					throw new Exception("Quantity is out of range.");
				}
				// TODO - Debug print
				System.out.println("Checked product quantity in the cart. Quantity = " + product.getQuantity());
			}
			
		} catch (JsonSyntaxException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		} catch (IDException eIdNotFound) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, eIdNotFound.getMessage());
			return;
		} catch (Exception eMalformed) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, eMalformed.getMessage());
			return;
		}
		
		/*
		 * Finding the products to add to the order
		 */
		for(Product product : cart.getProducts()) {
			try {
				Product productToAdd = productDao.getProductByIdAndSupplier(product.getId(), supplierId);
				productToAdd.setQuantity(product.getQuantity());
				userProducts.add(productToAdd);
			} catch (SQLException e) {
				ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch the product.");
				return;
			} catch(IDException e) {
				ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				return;
			}
		}
		
		/*
		 * Calc the total of the order
		 */
		try {
			total = CalcExpenses.calcTotal(userProducts, supplierDao.getSupplierById(supplierId));
		} catch (SQLException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch the supplier.");
			return;
		} catch(IDException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		
		/*
		 * Address of the order
		 */
		try {
			addressId = addressDao.getAddressIdByParameters(req.getParameter("citta"), req.getParameter("via"), req.getParameter("cap"), Integer.parseInt(req.getParameter("numero")));
		} catch (SQLException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch the address.");
			return;
		}
		
		/*
		 * Adding the order to the database
		 */
		try {
			orderDao.addOrder(total, addressId, ((User)session.getAttribute("user")).getId(), supplierId, userProducts);
		} catch (SQLException e) {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to add the order to the database.");
			return;
		}
		
		resp.setStatus(HttpServletResponse.SC_OK);
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
