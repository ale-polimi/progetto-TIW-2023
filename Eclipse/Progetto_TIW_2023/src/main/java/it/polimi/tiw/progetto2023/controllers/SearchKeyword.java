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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.dao.ProductDAO;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;

@WebServlet("/SearchKeyword")
public class SearchKeyword extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public SearchKeyword() {
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
		List<Product> productOffers = new ArrayList<Product>();
		List<Product> products = new ArrayList<>();
		
		if(req.getParameter("keyword") != null && !req.getParameter("keyword").equals("")) {
			try {
				products = productDao.getProductsByKeyword(req.getParameter("keyword"));
			} catch (SQLException e) {
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to load searched products.");
				return;
			}
		}
		
		req.getSession().setAttribute("productList", products);
		String path = "/WEB-INF/results.html";
		resp.setContentType("text");
		ServletContext servletContext = getServletContext();
		final WebContext webContext = new WebContext(req, resp, servletContext, req.getLocale());
		webContext.setVariable("offers", productOffers);
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
