package it.polimi.tiw.progetto2023.beans;

public class Address {

	private int id;
	private String citta;
	private String via;
	private String cap;
	private int numero;
	
	/**
	 * @return the id of the address.
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set for this address.
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the city of the address. 
	 */
	public String getCitta() {
		return citta;
	}
	/**
	 * @param citta the city to set for this address.
	 */
	public void setCitta(String citta) {
		this.citta = citta;
	}
	/**
	 * @return the road of the address.
	 */
	public String getVia() {
		return via;
	}
	/**
	 * @param via the road to set for this address.
	 */
	public void setVia(String via) {
		this.via = via;
	}
	/**
	 * @return the zip code of the address.
	 */
	public String getCap() {
		return cap;
	}
	/**
	 * @param cap the zip code to set for this address.
	 */
	public void setCap(String cap) {
		this.cap = cap;
	}
	/**
	 * @return the number of the address.
	 */
	public int getNumero() {
		return numero;
	}
	/**
	 * @param numero the number to set for this address.
	 */
	public void setNumero(int numero) {
		this.numero = numero;
	}
}
