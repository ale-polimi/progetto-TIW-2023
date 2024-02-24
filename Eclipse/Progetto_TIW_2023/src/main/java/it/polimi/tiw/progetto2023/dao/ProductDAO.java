package it.polimi.tiw.progetto2023.dao;

import java.sql.Connection;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Queue;

import it.polimi.tiw.progetto2023.beans.Product;
import it.polimi.tiw.progetto2023.utils.IDException;

public class ProductDAO {

	private Connection connection;

	public ProductDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Returns a product searched by its ID.
	 * @param id is the ID of the product.
	 * @return the product with that ID if it exists.
	 * @throws SQLException if the database can't be reached.
	 * @throws IDException if the ID does not exist in the database.
	 */
	public Product getProductById(int id) throws SQLException, IDException {
		Product product = new Product();
		String preparedQuery = "SELECT * FROM prodotto p JOIN vendita v ON v.IdProdotto=p.Id WHERE p.Id = ? AND v.Prezzo=(SELECT MIN(Prezzo) FROM vendita v1 WHERE v1.IdProdotto=v.IdProdotto)";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, id);
			
			result = pStatement.executeQuery();
			
			if(!result.isBeforeFirst()) {
				throw new IDException();
			} else if(result.next()) {
				product.setId(result.getInt("Id"));
				product.setName(result.getString("Nome"));
				product.setCategory(result.getString("Categoria"));
				product.setDescription(result.getString("Descrizione"));
				product.setPrice(result.getFloat("Prezzo"));
				Blob blobImage = result.getBlob("Immagine");
				byte[] imageData = blobImage.getBytes(1, (int)blobImage.length());
				String image = new String(Base64.getEncoder().encode(imageData));
				product.setImage(image);
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
		return product;
	}
	
	/**
	 * Returns a list of offers for the product searched by its ID.
	 * @param id is the ID of the product.
	 * @return the offers for that product with that ID if it exists.
	 * @throws SQLException if the database can't be reached.
	 * @throws IDException if the ID does not exist in the database.
	 */
	public List<Product> getProductOffersById(int id) throws SQLException, IDException {
		List<Product> products = new ArrayList<Product>();
		SupplierDAO supplierDao = new SupplierDAO(connection);
		String preparedQuery = "SELECT * FROM prodotto pr, vendita v, fornitore f WHERE pr.Id=v.IdProdotto AND f.Id=v.IdFornitore AND pr.Id = ? ORDER BY Prezzo";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, id);
			
			result = pStatement.executeQuery();
			
			if(!result.isBeforeFirst()) {
				throw new IDException();
			}
			
			while(result.next()) {
				Product product = new Product();
				product.setId(result.getInt("pr.Id"));
				product.setName(result.getString("pr.Nome"));
				product.setCategory(result.getString("pr.Categoria"));
				product.setDescription(result.getString("pr.Descrizione"));
				product.setPrice(result.getFloat("v.Prezzo"));
				product.setSupplier(supplierDao.getSupplierById(result.getInt("IdFornitore")));
				Blob blobImage = result.getBlob("pr.Immagine");
				byte[] imageData = blobImage.getBytes(1, (int)blobImage.length());
				String image = new String(Base64.getEncoder().encode(imageData));
				product.setImage(image);
				products.add(product);
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
		return products;
	}
	
	/**
	 * Returns a product searched with its ID and its supplier ID.
	 * @param productId is the ID of the product.
	 * @param supplierId is the ID of the supplier.
	 * @return the product with that ID and with that supplier if it exists.
	 * @throws SQLException if the database can't be reached.
	 * @throws IDException if the ID does not exist in the database.
	 */
	public Product getProductByIdAndSupplier(int productId, int supplierId) throws SQLException, IDException {
		Product product = new Product();
		SupplierDAO supplierDao = new SupplierDAO(connection);
		String preparedQuery = "SELECT * FROM prodotto p, vendita v, fornitore f WHERE p.Id=v.IdProdotto AND f.Id=v.IdFornitore AND p.Id = ? AND f.Id = ? ORDER BY Prezzo";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, productId);
			pStatement.setInt(2, supplierId);
			
			// TODO - Debug print
			System.out.println(pStatement);
			
			result = pStatement.executeQuery();
			
			if(!result.isBeforeFirst()) {
				throw new IDException();
			}
			while(result.next()) {
				product.setId(result.getInt("p.Id"));
				product.setName(result.getString("p.Nome"));
				product.setCategory(result.getString("p.Categoria"));
				product.setDescription(result.getString("p.Descrizione"));
				product.setPrice(result.getFloat("Prezzo"));
				product.setSupplier(supplierDao.getSupplierById(result.getInt("f.Id")));
				Blob blobImage = result.getBlob("Immagine");
				byte[] imageData = blobImage.getBytes(1, (int)blobImage.length());
				String image = new String(Base64.getEncoder().encode(imageData));
				product.setImage(image);
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
		return product;
	}
	
	/**
	 * Returns the products searched with a keyword. The keyword can be found in the name or in the description of the product.
	 * @param keyword is the keyword.
	 * @return the products that have the keyword in their name or description.
	 * @throws SQLException if the database can't be reached.
	 */
	public List<Product> getProductsByKeyword(String keyword) throws SQLException {
		List<Product> products = new ArrayList<Product>();
		String preparedKeyword = "%" + keyword + "%";
		String preparedQuery = "SELECT * FROM prodotto p JOIN vendita v ON p.Id=v.IdProdotto WHERE (Nome LIKE ? OR Descrizione LIKE ?) AND Prezzo=(SELECT MIN(Prezzo) FROM vendita v1 WHERE v.IdProdotto=v1.IdProdotto) ORDER BY Prezzo";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setString(1, preparedKeyword);
			pStatement.setString(2, preparedKeyword);
			
			result = pStatement.executeQuery();
			
			while(result.next()) {
				Product product = new Product();
				product.setId(result.getInt("Id"));
				product.setName(result.getString("Nome"));
				product.setCategory(result.getString("Categoria"));
				product.setDescription(result.getString("Descrizione"));
				product.setPrice(result.getFloat("Prezzo"));
				Blob blobImage = result.getBlob("Immagine");
				byte[] imageData = blobImage.getBytes(1, (int)blobImage.length());
				String image = new String(Base64.getEncoder().encode(imageData));
				product.setImage(image);
				products.add(product);
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
		return products;
	}
	
	/**
	 * Returns the products for the homepage.
	 * @param alreadyDisplayed is the queue of products that were already displayed to the user.
	 * @param quantity is the number of products that the DAO has to search in the database. This number varies from 1 to 5.
	 * @return a list of products that the user has not clicked on before.
	 * @throws SQLException if the database can't be reached.
	 */
	public List<Product> getProducts(Queue<Integer> alreadyDisplayed, int quantity) throws SQLException {
		List<Product> products = new ArrayList<Product>();
		String preparedQuery;
		boolean valid = alreadyDisplayed != null && !alreadyDisplayed.isEmpty();
		
		if(valid == true) {
			
			String notIn = "?";
			for(int i = 1; i < alreadyDisplayed.size(); i++) {
				notIn += ", ?";
			}
			
			preparedQuery = "SELECT * FROM prodotto p JOIN vendita v ON p.Id=v.IdProdotto WHERE Id NOT IN ("+notIn+") AND Prezzo=(SELECT MIN(Prezzo) FROM vendita v1 WHERE v.IdProdotto=v1.IdProdotto) ORDER BY RAND() LIMIT ?";
		} else {
			preparedQuery = "SELECT * FROM prodotto p JOIN vendita v ON p.Id=v.IdProdotto WHERE Prezzo=(SELECT MIN(Prezzo) FROM vendita v1 WHERE v.IdProdotto=v1.IdProdotto) ORDER BY RAND() LIMIT ?";
		}
		
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			//pStatement.setString(1, "Elettronica");
			
			if(valid == true) {
				int i = 0;
				Object[] array = alreadyDisplayed.toArray();
				while(i < alreadyDisplayed.size()) {
					pStatement.setInt(i + 1, (int)array[i]);
					i++;
				}
				pStatement.setInt(i + 1, quantity);
			} else {
				pStatement.setInt(1, quantity);
			}
			
			result = pStatement.executeQuery();
			
			int i = 0;
			while(result.next() && i < quantity) {
				Product product = new Product();
				product.setId(result.getInt("Id"));
				product.setName(result.getString("Nome"));
				product.setCategory(result.getString("Categoria"));
				product.setDescription(result.getString("Descrizione"));
				product.setPrice(result.getFloat("Prezzo"));
				Blob blobImage = result.getBlob("Immagine");
				byte[] imageData = blobImage.getBytes(1, (int)blobImage.length());
				String image = new String(Base64.getEncoder().encode(imageData));
				product.setImage(image);
				products.add(product);
				i++;
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
		return products;
	}
	
	/**
	 * Checks if a product exists in the database.
	 * @param id is the ID of the product.
	 * @return {@code true} if the product exists in the database, {@code false} otherwise.
	 * @throws SQLException if the database can't be reached.
	 */
	public boolean existsProduct(int id) throws SQLException {
		String preparedQuery = "SELECT * FROM prodotto p WHERE p.Id = ?";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, id);
			
			result = pStatement.executeQuery();
			
			if(!result.isBeforeFirst()) {
				return false;
			}
			return true;
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
	}
}
