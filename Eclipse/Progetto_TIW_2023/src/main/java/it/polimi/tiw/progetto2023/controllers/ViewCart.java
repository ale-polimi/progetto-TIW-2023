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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.progetto2023.beans.Cart;
import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.beans.User;
import it.polimi.tiw.progetto2023.dao.ProductDAO;
import it.polimi.tiw.progetto2023.dao.SupplierDAO;
import it.polimi.tiw.progetto2023.utils.CalcExpenses;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.CookieParser;
import it.polimi.tiw.progetto2023.utils.IDException;

@WebServlet("/ViewCart")
public class ViewCart extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public ViewCart() {
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
		List<Cart> cartsToShow = new ArrayList<Cart>();
		ProductDAO productDao = new ProductDAO(connection);
		SupplierDAO supplierDao = new SupplierDAO(connection);
		HttpSession session = req.getSession();
		Cookie[] cookies = req.getCookies();
		List<Product> cartProducts = new ArrayList<>();
		
		if(cookies != null) {
			for(int i = 0; i < cookies.length; i++) {
				List<Product> supplierList = new ArrayList<Product>();
				if(!cookies[i].getName().equals("JSESSIONID")) {
					if(cookies[i].getName().split("-")[0].equals(String.valueOf(((User)session.getAttribute("user")).getId()))) {
						Cart cart = new Cart();
						try {
							cart.setSupplier(supplierDao.getSupplierById(Integer.parseInt(cookies[i].getName().split("-")[1])));
						} catch (SQLException e) {
							resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to load supplier from cookie.");
							return;
						} catch (IDException e) {
							resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
							return;
						}
						
						// TODO - Debug print
						System.out.println("Called cookie parser from: " + this.getClass());
						
						cartProducts = CookieParser.parseCookie(cookies[i]);
						for(Product product : cartProducts) {
							try {
								Product productToAdd = productDao.getProductByIdAndSupplier(product.getId(), product.getSupplier().getId());
								productToAdd.setQuantity(product.getQuantity());
								supplierList.add(productToAdd);
							} catch (SQLException e) {
								resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch products from cookie.");
								return;
							} catch (IDException e) {
								resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
								return;
							}
						}
						cart.setProducts(supplierList);
						cartsToShow.add(cart);
					}
				}
			}
		}
		
		for(Cart cart : cartsToShow) {
			cart.setShippingCost(CalcExpenses.calcShippingPrice(cart.getProducts(), cart.getSupplier()));
			cart.setTotalCost(CalcExpenses.calcPrice(cart.getProducts()));
		}
		
		String path = "/WEB-INF/cart.html";
		ServletContext servletContext = getServletContext();
		final WebContext webContext = new WebContext(req, resp, servletContext, req.getLocale());
		webContext.setVariable("suppliers", cartsToShow);
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
