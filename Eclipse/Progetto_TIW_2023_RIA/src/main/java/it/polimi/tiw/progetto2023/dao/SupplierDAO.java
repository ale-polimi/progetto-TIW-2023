package it.polimi.tiw.progetto2023.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.progetto2023.beans.ShippingRange;
import it.polimi.tiw.progetto2023.beans.Supplier;
import it.polimi.tiw.progetto2023.utils.IDException;

public class SupplierDAO {

	private Connection connection;

	public SupplierDAO(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Returns the supplier with the specified ID.
	 * @param id is the ID of the supplier.
	 * @return the supplier identified by that ID.
	 * @throws SQLException if the database can't be reached.
	 * @throws IDException if the ID does not exist in the database.
	 */
	public Supplier getSupplierById(int id) throws SQLException, IDException {
		String preparedQuery = "SELECT * from fornitore f JOIN politicaSpedizione ps ON ps.Id=f.IdPoliticaSpedizione WHERE f.Id = ?";
		Supplier supplier = new Supplier();
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		/*
		 * Finding the supplier
		 */
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, id);
			
			result = pStatement.executeQuery();
			
			if(!result.isBeforeFirst()) {
				throw new IDException();
			}
			while(result.next()) {
				supplier.setName(result.getString("Nome"));
				supplier.setEvaluation(result.getString("Valutazione"));
				supplier.setId(result.getInt("f.Id"));
				supplier.setFreeShippingLimit(result.getInt("Soglia"));
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
		 * Finding the shipping rates of this supplier
		 */
		List<ShippingRange> shippingRanges = new ArrayList<ShippingRange>();
		preparedQuery = "SELECT * FROM fornitore fo, politicaSpedizione ps1, fascia fa, composizioneFasce cf WHERE ps1.Id=fo.IdpoliticaSpedizione AND fa.IdFascia=cf.IdFasceComp AND cf.IdPoliticaComp=ps1.Id AND fo.Id = ?";
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, id);
			
			result = pStatement.executeQuery();
			
			while(result.next()) {
				ShippingRange shippingRange = new ShippingRange(result.getInt("IdFascia"), result.getInt("Min"), result.getInt("Max"), result.getInt("Prezzo"));
				shippingRanges.add(shippingRange);
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
		
		supplier.setPoliticaSpedizione(shippingRanges);
		
		return supplier;
	}
	
	/**
	 * Checks if a supplier exists in the database.
	 * @param id is the ID of the supplier.
	 * @return {@code true} if the supplier with the specified ID exists in the database, {@code false} otherwise.
	 * @throws SQLException if the database can't be reached.
	 */
	public boolean existsSupplier(int id) throws SQLException {
		String preparedQuery = "SELECT * FROM fornitore f WHERE f.Id = ?";
		ResultSet result = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = connection.prepareStatement(preparedQuery);
			
			pStatement.setInt(1, id);
			
			result = pStatement.executeQuery();
			
			// Can't find supplier
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
