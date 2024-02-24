package it.polimi.tiw.progetto2023.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.progetto2023.beans.Order;
import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.utils.CalcExpenses;
import it.polimi.tiw.progetto2023.utils.IDException;

public class OrderDAO {

	private Connection connection;

	public OrderDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Returns the list of orders of the user specified by the ID.
	 * @param id is the ID of the user.
	 * @return the list of orders with the products for each order of the user.
	 * @throws SQLException if the database can't be reached.
	 * @throws IDException if the ID does not exist in the database.
	 */
	public List<Order> getOrdersOfUserById(int id) throws SQLException, IDException {
		List<Order> orders = new ArrayList<Order>();
		List<Integer> ordersId = new ArrayList<Integer>();
		String preparedQuery = "SELECT * FROM ordine o WHERE o.IdUtente = ? ORDER BY Data DESC";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		/*
		 * First we find all the IDs of the orders of the user...
		 */
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, id);
			
			result = pStatement.executeQuery();
			
			while(result.next()) {
				ordersId.add(result.getInt("Id"));
			}
		} finally {
			try {
				if (result != null)
					result.close();
			} catch (SQLException e1) {
				throw e1;
			}
			try {
				if (pStatement != null)
					pStatement.close();
			} catch (SQLException e2) {
				throw e2;
			}
		}
		
		/*
		 * ... then we find the products in each order.
		 */
		SupplierDAO supplierDao = new SupplierDAO(connection);
		ProductDAO productDao = new ProductDAO(connection);
		AddressDAO addressDao = new AddressDAO(connection);
		Date date = new Date();
		int supplierId = -1;
		int addressId = -1;
		
		for(Integer orderId : ordersId) {
			Order order = new Order();
			preparedQuery = "SELECT * FROM ordine o JOIN contenuto c ON o.Id=c.IdOrdine WHERE o.Id = ?";
			
			try {
				pStatement = connection.prepareStatement(preparedQuery);
				
				pStatement.setInt(1, orderId);
				
				// TODO - Debug print
				System.out.println(pStatement);
				
				result = pStatement.executeQuery();
				
				if(!result.isBeforeFirst()) {
					throw new IDException();
				}
				
				List<Product> products = new ArrayList<Product>();
				while(result.next()) {
					supplierId = result.getInt("IdFornitore");
					Product product = productDao.getProductByIdAndSupplier(result.getInt("IdProdotto"), supplierId);
					product.setQuantity(result.getInt("Quantita"));
					products.add(product);
					date = new Date(result.getTimestamp("Data").getTime());
					addressId = result.getInt("IdIndirizzo");
				}
				
				order.setId(orderId);
				order.setTotal(CalcExpenses.calcTotal(products, supplierDao.getSupplierById(supplierId)));
				order.setProducts(products);
				order.setSupplier(supplierDao.getSupplierById(supplierId));
				order.setDate(date);
				order.setAddress(addressDao.getAddressById(addressId));
			} finally {
				try {
					if (result != null)
						result.close();
				} catch (SQLException e1) {
					throw e1;
				}
				try {
					if (pStatement != null)
						pStatement.close();
				} catch (SQLException e2) {
					throw e2;
				}
			}
			
			orders.add(order);
		}
		return orders;
	}
	
	/**
	 * Creates an order in the database.
	 * @param total is the total cost of the order.
	 * @param addressId is the ID of the shipping address.
	 * @param userId is the ID of the user.
	 * @param supplierId is the ID of the supplier for this order.
	 * @param products is the list of products in this order.
	 * @throws SQLException if the database can't be reached.
	 */
	public void addOrder(float total, int addressId, int userId, int supplierId, List<Product> products) throws SQLException {
		int newOrderId = getNextId();
		String preparedQuery = "INSERT INTO ordine (Id, Totale, Data, IdIndirizzo, IdUtente, IdFornitore) VALUES ( ? , ? , CURRENT_TIMESTAMP(), ? , ? , ? )";
		PreparedStatement pStatement = null;
		
		connection.setAutoCommit(false);
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, newOrderId);
			pStatement.setFloat(2, total);
			pStatement.setInt(3, addressId);
			pStatement.setInt(4, userId);
			pStatement.setInt(5, supplierId);
			
			pStatement.executeUpdate();
			
			for(Product product : products) {
				preparedQuery = "INSERT INTO contenuto (IdOrdine, IdProdotto, Quantita) VALUES ( ? , ? , ? )";
				PreparedStatement pStatement2 = null;
				
				try {
					pStatement2 = connection.prepareStatement(preparedQuery);
					
					pStatement2.setInt(1, newOrderId);
					pStatement2.setInt(2, product.getId());
					pStatement2.setInt(3, product.getQuantity());
					pStatement2.executeUpdate();
				} finally {
					try {
						if (pStatement2 != null)
							pStatement2.close();
					} catch (SQLException e2) {
						throw e2;
					}
				}
			}
			
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			
			connection.setAutoCommit(true);
			
			try {
				if (pStatement != null)
					pStatement.close();
			} catch (SQLException e2) {
				throw e2;
			}
		}
	}

	/**
	 * Returns the next available ID for the orders in the database.
	 * @return the next available ID for the orders in the database.
	 * @throws SQLException if the database can't be reached.
	 */
	private int getNextId() throws SQLException {
		String preparedQuery = "SELECT MAX(Id) AS Id FROM ordine";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			result = pStatement.executeQuery();
			
			if(!result.isBeforeFirst()) {
				return 1;
			}
			if(result.next()) {
				return result.getInt("Id") + 1;
			}
		} finally {
			try {
				if (result != null)
					result.close();
			} catch (SQLException e1) {
				throw e1;
			}
			try {
				if (pStatement != null)
					pStatement.close();
			} catch (SQLException e2) {
				throw e2;
			}
		}
		return -1;
	}
}
