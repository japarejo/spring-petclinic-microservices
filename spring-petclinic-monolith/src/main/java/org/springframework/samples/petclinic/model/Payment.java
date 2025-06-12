package org.springframework.samples.petclinic.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;



@Entity
public class Payment extends AuditableEntity{
	double amount;
	
	@OneToOne
	Owner owner;
	
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public Owner getOwner() {
		return owner;
	}
	public void setOwner(Owner owner) {
		this.owner = owner;
	}
	
}
