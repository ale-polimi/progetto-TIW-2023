package it.polimi.tiw.progetto2023.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CheckLogin implements Filter {

	public CheckLogin() {
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginPath = req.getServletContext().getContextPath() + "/login.html";
		HttpSession session = req.getSession();
		if(session.isNew() || session.getAttribute("user") == null) {
			res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			res.setHeader("Location", loginPath);
			return;
		}
		filterChain.doFilter(request, response);
	}
}
