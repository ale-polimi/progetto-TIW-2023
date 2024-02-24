package it.polimi.tiw.progetto2023.beans;

public class User {
	
	private int id;
	private String email;
	private String name;
	private String surname;
	private Address address;
	
	/**
	 * Getter method for the user's ID.
	 * @return the user's ID.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Setter method for the ID.
	 * @param id is the ID for the user.
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Getter method for the user's email.
	 * @return the user's email.
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Setter method for the email.
	 * @param email is the email for the user.
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Getter method for the user's name.
	 * @return the user's name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Setter method for the name.
	 * @param name is the name for the user.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Getter method for the user's surname.
	 * @return the user's surname.
	 */
	public String getSurname() {
		return surname;
	}
	
	/**
	 * Setter method for the surname.
	 * @param surname is the surname for the user
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	/**
	 * Getter method for the user's address.
	 * @return the user's address.
	 */
	public Address getAddress() {
		return address;
	}
	
	/**
	 * Setter method for the address.
	 * @param address is the address for the user.
	 */
	public void setAddress(Address address) {
		this.address = address;
	}
}
