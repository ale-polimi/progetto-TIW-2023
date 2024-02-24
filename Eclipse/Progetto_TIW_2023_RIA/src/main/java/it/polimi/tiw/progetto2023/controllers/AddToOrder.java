package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.beans.User;
import it.polimi.tiw.progetto2023.dao.AddressDAO;
import it.polimi.tiw.progetto2023.dao.OrderDAO;
import it.polimi.tiw.progetto2023.dao.ProductDAO;
import it.polimi.tiw.progetto2023.dao.SupplierDAO;
import it.polimi.tiw.progetto2023.utils.CalcExpenses;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.CookieParser;
import it.polimi.tiw.progetto2023.utils.IDException;

@WebServlet("/AddToOrder")
public class AddToOrder extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public AddToOrder() {
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
		OrderDAO orderDao = new OrderDAO(connection);
		ProductDAO productDao = new ProductDAO(connection);
		SupplierDAO supplierDao = new SupplierDAO(connection);
		AddressDAO addressDao = new AddressDAO(connection);
		
		if(req.getParameter("supplierId") != null) {
			int supplierId = Integer.parseInt(req.getParameter("supplierId"));
			
			try {
				if(!supplierDao.existsSupplier(supplierId)) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The specified ID does not exist.");
					return;
				}
				
				if(req.getParameter("citta") == null || req.getParameter("citta") == "" ||
				   req.getParameter("via") == null || req.getParameter("via") == "" ||
				   req.getParameter("cap") == null || req.getParameter("cap") == "" ||
				   req.getParameter("numero") == null || req.getParameter("numero") == "") {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Address fields can't be empty.");
					return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			HttpSession session = req.getSession();
			List<Product> userProducts = new ArrayList<Product>();
			float total = -1;
			int addressId = -1;
			List<Product> productsFromCookie = CookieParser.getProductsBySupplierAndUser(((User)session.getAttribute("user")).getId(), supplierId, req.getCookies());
			Cookie cookie = new Cookie(String.valueOf(((User)session.getAttribute("user")).getId()) + "-" + String.valueOf(supplierId), "");
			cookie.setMaxAge(0);
			resp.addCookie(cookie);
			
			for(Product product : productsFromCookie) {
				try {
					Product productToAdd = productDao.getProductByIdAndSupplier(product.getId(), product.getSupplier().getId());
					productToAdd.setQuantity(product.getQuantity());
					userProducts.add(productToAdd);
				} catch (SQLException e) {
					resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch the product.");
					return;
				} catch(IDException e) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
					return;
				}
			}
			
			/*
			 * Calc the total of the order
			 */
			try {
				total = CalcExpenses.calcTotal(userProducts, supplierDao.getSupplierById(supplierId));
			} catch (SQLException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch the supplier.");
				return;
			} catch(IDException e) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				return;
			}
			
			/*
			 * Address of the order
			 */
			try {
				addressId = addressDao.getAddressIdByParameters(req.getParameter("citta"), req.getParameter("via"), req.getParameter("cap"), Integer.parseInt(req.getParameter("numero")));
			} catch (SQLException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch the address.");
				return;
			}
			
			/*
			 * Adding the order to the database
			 */
			try {
				orderDao.addOrder(total, addressId, ((User)session.getAttribute("user")).getId(), supplierId, userProducts);
			} catch (SQLException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to add the order to the database.");
				return;
			}
			
			String path = getServletContext().getContextPath() + "/ViewOrders";
			resp.sendRedirect(path);
			
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter.");
			return;
		}
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
