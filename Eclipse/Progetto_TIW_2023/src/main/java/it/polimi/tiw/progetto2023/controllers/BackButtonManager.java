package it.polimi.tiw.progetto2023.controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/BackButtonManager")
public class BackButtonManager extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public BackButtonManager() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String previousPage;
		
		previousPage = req.getParameter("previousPage");
		
		if(previousPage == null || previousPage.isBlank()) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "The previous page can't be empty.");
			return;
		}
		
		String path;
		path = getServletContext().getContextPath() + previousPage;
		resp.sendRedirect(path);
	}
}
