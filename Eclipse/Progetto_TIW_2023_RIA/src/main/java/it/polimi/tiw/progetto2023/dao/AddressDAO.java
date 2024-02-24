package it.polimi.tiw.progetto2023.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.polimi.tiw.progetto2023.beans.Address;
import it.polimi.tiw.progetto2023.utils.IDException;

public class AddressDAO {

	private Connection connection;

	public AddressDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Returns the address with the specified ID.
	 * @param id is the ID of the address.
	 * @return the address with the specified ID, if it exists.
	 * @throws SQLException if the database can't be reached.
	 * @throws IDException if the ID does not exist in the database.
	 */
	public Address getAddressById(int id) throws SQLException, IDException {
		Address address = new Address();
		String preparedQuery = "SELECT * FROM indirizzo i WHERE i.Id = ?";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, id);
			
			result = pStatement.executeQuery();
			
			// No results
			if(!result.isBeforeFirst()) {
				throw new IDException();
			}
			if(result.next()) {
				address.setId(id);
				address.setCitta(result.getString("Citta"));
				address.setVia(result.getString("Via"));
				address.setCap(result.getString("Cap"));
				address.setNumero(result.getInt("Numero"));
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
		
		return address;
	}
	
	/**
	 * Returns the ID of an address given its parameters.
	 * @param citta is the city of the address.
	 * @param via is the road of the address.
	 * @param cap is the zip code of the address.
	 * @param numero is the number of the address.
	 * @return the ID of the address.
	 * @throws SQLException if the database can't be reached.
	 */
	public int getAddressIdByParameters(String citta, String via, String cap, int numero) throws SQLException {
		int returnId = -1;
		String preparedQuery = "SELECT i.Id FROM indirizzo i WHERE i.Citta = ? AND i.Via = ? AND i.Cap = ? AND i.Numero = ?";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setString(1, citta);
			pStatement.setString(2, via);
			pStatement.setString(3, cap);
			pStatement.setInt(4, numero);
			
			result = pStatement.executeQuery();
			
			if(result.next()) {
				returnId = result.getInt("Id");
			} else {
				// Create a new address and return its new ID
				addAddress(citta, via, cap, numero);
				return getAddressIdByParameters(citta, via, cap, numero);
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
		
		return returnId;
	}
	
	/**
	 * Adds an address to the database.
	 * @param citta is the city of the address.
	 * @param via is the road of the address.
	 * @param cap is the zip code of the address.
	 * @param numero is the number of the address.
	 * @throws SQLException if the database can't be reached.
	 */
	private void addAddress(String citta, String via, String cap, int numero) throws SQLException {
		String preparedQuery = "INSERT INTO indirizzo (Citta, Via, Cap, Numero) VALUES (?, ?, ?, ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setString(1, citta);
			pStatement.setString(2, via);
			pStatement.setString(3, cap);
			pStatement.setInt(4, numero);
			
			pStatement.executeUpdate();
		} finally {
			try {
				if (pStatement != null)
					pStatement.close();
			} catch (SQLException e2) {
				throw e2;
			}
		}
	}
}
