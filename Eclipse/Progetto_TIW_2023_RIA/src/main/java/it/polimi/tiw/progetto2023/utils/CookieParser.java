package it.polimi.tiw.progetto2023.utils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.beans.User;

public class CookieParser {

	/**
	 * Parses a cookie to retrieve the cart products of the user.
	 * @param cookie is the cookie of the user.
	 * @return the list of cart products for the user.
	 */
	public static List<Product> parseCookie(Cookie cookie){
		List<Product> cartProducts = new ArrayList<Product>();
		
		if(cookie != null) {
			String supplierId = cookie.getName().split("-")[1];
			String value = cookie.getValue();
			String products[] = value.split("_");
			
			for(int i = 0; i < products.length; i++) {
				String info[] = products[i].split("-");
				Product product = new Product();
				product.setId(Integer.parseInt(info[0]));
				product.setQuantity(Integer.parseInt(info[1]));
				product.getSupplier().setId(Integer.parseInt(supplierId));
				cartProducts.add(product);
			}
		}
		return cartProducts;
	}
	
	/**
	 * Retrieves all the products of an user for a supplier already present in the cookies.
	 * @param userId is the user ID.
	 * @param supplierId is the supplier ID.
	 * @param cookies are the cookies of the user.
	 * @return the list of products of that user for that supplier already present in the cookies.
	 */
	public static List<Product> getProductsBySupplierAndUser(int userId, int supplierId, Cookie[] cookies){
		List<Product> cartProducts = new ArrayList<Product>();
		
		if(cookies != null) {
			for(int i = 0; i < cookies.length; i++) {
				if(!cookies[i].getName().equals("JSESSIONID")) {
					if(cookies[i].getName().split("-")[0].equals(String.valueOf(userId))) {
						if(cookies[i].getName().split("-")[1].equals(String.valueOf(supplierId))) {
							cartProducts = CookieParser.parseCookie(cookies[i]);
						}
					}
				}
			}
		}
		return cartProducts;
	}
	
	/**
	 * Creates a new cookie for the user containing all the products. Used in AddToCart
	 * @param products is the list of products.
	 * @param request is the HTTP request.
	 * @return a new cookie containing the user ID and supplier ID as {@code name} and the product IDs and quantities as {@code value}.
	 */
	public static Cookie createCookieByProducts(List<Product> products, HttpServletRequest request) {
		if(products.size() != 0) {
			HttpSession session = request.getSession();
			String name = ((User)session.getAttribute("user")).getId() + "-" + String.valueOf(products.get(0).getSupplier().getId());
			boolean first = true;
			String value = "";
			for(Product product : products) {
				value += ((first) ? "" : "_") + product.getId() + "-" + product.getQuantity();
				first = false;
			}
			Cookie cookie = new Cookie(name, value);
			return cookie;
		} else {
			return null;
		}
	}
}
