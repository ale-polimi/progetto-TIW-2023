package it.polimi.tiw.progetto2023.utils;

import java.util.List;

import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.beans.ShippingRange;
import it.polimi.tiw.progetto2023.beans.Supplier;

public class CalcExpenses {

	/**
	 * Finds the total price, shipping included of the products bought from the supplier.
	 * @param products is the list of products.
	 * @param supplier is the supplier.
	 * @return the total price of the products, shipping included.
	 */
	public static float calcTotal(List<Product> products, Supplier supplier) {
		return calcPrice(products) + calcShippingPrice(products, supplier);
	}
	
	/**
	 * Finds the price of the shipping cost of the products from the supplier.
	 * @param products is the list of products.
	 * @param supplier is the supplier.
	 * @return he price of the shipping cost of the products from the supplier.
	 */
	public static int calcShippingPrice(List<Product> products, Supplier supplier) {
		int shippingPrice = 0;
		int numberOfProducts = calcNumOfProducts(products);
		float total = calcPrice(products);
		
		if(supplier.getFreeShippingLimit() == -1 || total <= supplier.getFreeShippingLimit()) {
			for(ShippingRange shippingRange : supplier.getPoliticaSpedizione()) {
				if((numberOfProducts >= shippingRange.getMin() && numberOfProducts <= shippingRange.getMax()) || (numberOfProducts >= shippingRange.getMin() && shippingRange.getMax() == 0)) {
					shippingPrice = shippingRange.getPrice();
					break;
				}
			}
		}
		
		return shippingPrice;
	}
	
	/**
	 * Finds the total price of the products.
	 * @param products is the list of products.
	 * @return the total price of the products.
	 */
	public static float calcPrice(List<Product> products) {
		float total = 0;
		
		for(Product product : products) {
			total += product.getPrice() * product.getQuantity();
		}
		
		return total;
	}
	
	/**
	 * Finds the total number of products bought in an order.
	 * @param products is the list of products.
	 * @return the total number of products bought in an order.
	 */
	public static int calcNumOfProducts(List<Product> products) {
		int numOfProducts = 0;
		
		for(Product product : products) {
			numOfProducts += product.getQuantity();
		}
		
		return numOfProducts;
	}
}
