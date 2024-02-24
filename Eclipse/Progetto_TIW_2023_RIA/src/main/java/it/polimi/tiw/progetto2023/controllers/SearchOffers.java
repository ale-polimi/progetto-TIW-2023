package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.beans.User;
import it.polimi.tiw.progetto2023.dao.ProductDAO;
import it.polimi.tiw.progetto2023.utils.CalcExpenses;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.CookieParser;
import it.polimi.tiw.progetto2023.utils.IDException;

@WebServlet("/SearchOffers")
public class SearchOffers extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public SearchOffers() {
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

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ProductDAO productDao = new ProductDAO(connection);
		List<Product> offers = new ArrayList<Product>();
		ServletContext servletContext = req.getServletContext();
		final WebContext webContext = new WebContext(req, resp, servletContext, req.getLocale());
		
		List<Product> productsList = (List<Product>) req.getSession().getAttribute("productList");
		if(req.getParameter("productId") != null) {
			
			/*
			 * 
			 */
			boolean present = false;
			if(productsList != null) {
				for(Product product : productsList) {
					if(product.getId() == Integer.parseInt(req.getParameter("productId"))) {
						present = true;
						break;
					}
				}
			}
			
			/*
			 * 
			 */
			if(!present) {
				List<Product> products = new ArrayList<>();
				try {
					products.add(productDao.getProductById(Integer.parseInt(req.getParameter("productId"))));
					req.getSession().setAttribute("productList", products);
				} catch(SQLException e) {
					e.printStackTrace();
				} catch(IDException e) {
					resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
					return;
				}
			}
			
			/*
			 * Adding the product to the previously viewed products.
			 */
			int productId = Integer.parseInt(req.getParameter("productId"));
			Queue<Integer> visualisedList = new LinkedList<>();
			HttpSession session = req.getSession();
			if(session.getAttribute("listaVisualizzati") == null) {
				visualisedList.add(productId);
				session.setAttribute("listaVisualizzati", visualisedList);
			} else {
				visualisedList = (Queue<Integer>) session.getAttribute("listaVisualizzati");
				if(!visualisedList.contains(productId)) {
					if(visualisedList.size() == 5) {
						visualisedList.remove();
					}
					visualisedList.add(productId);
				}
			}
			webContext.setVariable("idToShow", req.getParameter("productId"));
			
			try {
				offers = productDao.getProductOffersById(Integer.parseInt(req.getParameter("productId")));
			} catch(SQLException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch products from ID.");
				return;
			} catch(IDException e) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				return;
			}
			
			Cookie[] cookies = req.getCookies();
			List<Product> products = new ArrayList<Product>();
			List<Product> cartProducts  = new ArrayList<Product>();
			for(Product product : offers) {
				int supplierId = product.getSupplier().getId();
				product.setValue(0);
				product.setQuantity(0);
				if(cookies != null) {
					for(int i = 0; i < cookies.length; i++) {
						cartProducts = new ArrayList<Product>();
						if(!cookies[i].getName().equals("JSESSIONID")) {
							if(cookies[i].getName().split("-")[0].equals(String.valueOf(((User)session.getAttribute("user")).getId())) && cookies[i].getName().split("-")[1].equals(String.valueOf(supplierId))){
								
								// TODO - Debug print
								System.out.println("Called cookie parser from: " + this.getClass());
								
								products = CookieParser.parseCookie(cookies[i]);
								for(Product p : products) {
									try {
										Product productToAdd = productDao.getProductByIdAndSupplier(p.getId(), p.getSupplier().getId());
										productToAdd.setQuantity(p.getQuantity());
										cartProducts.add(productToAdd);
									} catch (SQLException e) {
										resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to fetch products from cookie.");
										return;
									} catch (IDException e) {
										resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
										return;
									}
								}
								product.setValue(CalcExpenses.calcPrice(cartProducts));
								product.setQuantity(CalcExpenses.calcNumOfProducts(cartProducts));
							}
						}
					}
				}
			}
		} else {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter.");
			return;
		}
		
		List<Product> products = (List<Product>) req.getSession().getAttribute("productList");
		String path = "/WEB-INF/results.html";
		resp.setContentType("text");
		webContext.setVariable("offers", offers);
		webContext.setVariable("products", products);
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
