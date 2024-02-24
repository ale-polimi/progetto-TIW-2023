package it.polimi.tiw.progetto2023.beans;

import java.util.ArrayList;
import java.util.List;

public class Supplier {

	private int id;
	private String name;
	private String evaluation;
	private int freeShippingLimit;
	private List<ShippingRange> politicaSpedizione;
	
	public Supplier() {
		this.politicaSpedizione = new ArrayList<ShippingRange>();
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the evaluation
	 */
	public String getEvaluation() {
		return evaluation;
	}

	/**
	 * @param evaluation the evaluation to set
	 */
	public void setEvaluation(String evaluation) {
		this.evaluation = evaluation;
	}

	/**
	 * @return the freeShippingLimit
	 */
	public int getFreeShippingLimit() {
		return freeShippingLimit;
	}

	/**
	 * @param freeShippingLimit the freeShippingLimit to set
	 */
	public void setFreeShippingLimit(int freeShippingLimit) {
		this.freeShippingLimit = freeShippingLimit;
	}

	/**
	 * @return the politicaSpedizione
	 */
	public List<ShippingRange> getPoliticaSpedizione() {
		return politicaSpedizione;
	}

	/**
	 * @param politicaSpedizione the politicaSpedizione to set
	 */
	public void setPoliticaSpedizione(List<ShippingRange> politicaSpedizione) {
		this.politicaSpedizione = politicaSpedizione;
	}
}
