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

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import com.google.gson.Gson;

import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.dao.ProductDAO;
import it.polimi.tiw.progetto2023.utils.ConnectionManager;
import it.polimi.tiw.progetto2023.utils.ServletErrorResponse;

@WebServlet("/SearchKeyword")
@MultipartConfig
public class SearchKeyword extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public SearchKeyword() {
		super();
	}
	
	@Override
	public void init() throws ServletException {
		connection = ConnectionManager.getConnection(getServletContext());
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ProductDAO productDao = new ProductDAO(connection);
		List<Product> products = new ArrayList<>();
		
		// TODO - Debug print
		System.out.println("Is request multipart? " + ServletFileUpload.isMultipartContent(req));
		
		if(req.getParameter("keyword") != null && !req.getParameter("keyword").equals("")) {
			try {
				products = productDao.getProductsByKeyword(req.getParameter("keyword"));
			} catch (SQLException e) {
				ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database can't be reached, unable to load searched products.");
				return;
			}
		} else {
			ServletErrorResponse.createResponse(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing parameter.");
			return;
		}
		
		Gson gson = new Gson();
		String jsonProducts = gson.toJson(products);
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(jsonProducts);
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
