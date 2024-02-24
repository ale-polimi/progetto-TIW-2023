package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
import it.polimi.tiw.progetto2023.dao.ProductDAO;
import it.polimi.tiw.progetto2023.dao.SupplierDAO;
import it.polimi.tiw.progetto2023.utils.CalcExpenses;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.CookieParser;

@WebServlet("/AddToCart")
public class AddToCart extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public AddToCart() {
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
		ProductDAO productDao = new ProductDAO(connection);
		SupplierDAO supplierDao = new SupplierDAO(connection);
		
		if(req.getParameter("supplierId") != null && req.getParameter("productId") != null) {
			int supplierId = Integer.parseInt(req.getParameter("supplierId"));
			int productId = Integer.parseInt(req.getParameter("productId"));
			
			try {
				if(!productDao.existsProduct(productId) || !supplierDao.existsSupplier(supplierId)) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The specified ID does not exist.");
					return;
				}
			} catch (SQLException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to check if the product or supplier exists.");
				return;
			}
			
			if(req.getParameter("quantity") == null || req.getParameter("quantity") == "") {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing quantity.");
				return;
			}
			
			boolean first = true;
			Cookie[] cookies = req.getCookies();
			HttpSession session = req.getSession();
			int quantity = Integer.parseInt(req.getParameter("quantity"));
			
			if(quantity < 1) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Quantity can't be less than 1.");
				return;
			}else if(quantity > 999) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Quantity can't be greater than 999.");
				return;
			} else if(cookies != null) {
				for(int i = 0; i < cookies.length; i++) {
					if(cookies[i].getName().split("-")[0].equals(String.valueOf(((User)session.getAttribute("user")).getId())) && cookies[i].getName().split("-")[1].equals(String.valueOf(supplierId))) {
						first = false;
						
						// TODO - Debug print
						System.out.println("Called cookie parser from: " + this.getClass());
						
						List<Product> cartProducts = CookieParser.parseCookie(cookies[i]);
						if(CalcExpenses.calcNumOfProducts(cartProducts) + quantity > 999) {
							resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Quantity of the products in the cart can't be greater than 999.");
							return;
						} else {
							boolean present = false;
							for(Product product : cartProducts) {
								if(product.getId() == productId) {
									product.setQuantity(product.getQuantity() + quantity);
									present = true;
									break;
								}
							}
							
							if(present) { // Create a new cookie
								Cookie cookie = CookieParser.createCookieByProducts(cartProducts, req);
								cookie.setMaxAge(3600);
								resp.addCookie(cookie);
							} else { // Update the cookie
								String value = cookies[i].getValue();
								value += "_" + String.valueOf(productId) + "-" + String.valueOf(quantity);
								Cookie cookie = new Cookie(cookies[i].getName(), value);
								cookie.setMaxAge(3600);
								resp.addCookie(cookie);
							}
							break;
						}
					}
				}
			}
			
			/*
			 * It's the first cookie for the user.
			 */
			if(first) {
				String supplId = req.getParameter("supplierId");
				String name = ((User)session.getAttribute("user")).getId() + "-" + supplId;
				String value = req.getParameter("productId") + "-" + req.getParameter("quantity");
				Cookie cookie = new Cookie(name, value);
				cookie.setMaxAge(3600);
				resp.addCookie(cookie);
			}
			
			String path = getServletContext().getContextPath() + "/ViewCart";
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
