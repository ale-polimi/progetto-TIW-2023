package it.polimi.tiw.progetto2023.dao;

import it.polimi.tiw.progetto2023.beans.Address;
import it.polimi.tiw.progetto2023.beans.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

	private Connection connection;
	
	public UserDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Checks the credentials of an user.
	 * @param email is the email of the user.
	 * @param password is the password of the user.
	 * @return the user associated to those credentials, {@code null} otherwise.
	 * @throws SQLException if the database can't be reached.
	 */
	public User checkCredentials(String email, String password) throws SQLException {
		String preparedQuery = "SELECT * FROM utente u join indirizzo i ON i.Id=u.IdIndirizzo WHERE Email = ? AND Password = ?";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setString(1, email);
			pStatement.setString(2, password);
			
			result = pStatement.executeQuery();
			
			// No results
			if(!result.isBeforeFirst()) {
				return null;
			} else {
				result.next();
				User user = new User();
				Address address = new Address();
				user.setId(result.getInt("IdUtente"));
				user.setName(result.getString("Nome"));
				user.setSurname(result.getString("Cognome"));
				address.setId(result.getInt("IdIndirizzo"));
				address.setCitta(result.getString("Citta"));
				address.setVia(result.getString("Via"));
				address.setCap(result.getString("Cap"));
				address.setNumero(result.getInt("Numero"));
				user.setAddress(address);
				
				return user;
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
	}
	
	/**
	 * Adds an user to the database.
	 * @param name is the name of the user.
	 * @param surname is the surname of the user.
	 * @param email is the email of the user.
	 * @param password is the password of the user.
	 * @param addressId is the ID of the address of the user.
	 * @throws SQLException if the database can't be reached.
	 */
	public void addUser(String name, String surname, String email, String password, int addressId) throws SQLException {
		String preparedQuery = "INSERT INTO utente (Nome, Cognome, Email, Password, IdIndirizzo) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setString(1, name);
			pStatement.setString(2, surname);
			pStatement.setString(3, email);
			pStatement.setString(4, password);
			pStatement.setInt(5, addressId);
			
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
