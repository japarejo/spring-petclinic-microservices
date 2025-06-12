package org.springframework.samples.petclinic.model;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "researchers")
public class Researcher extends NamedEntity {

	@ManyToMany
	Set<Researcher> phdAdvisors;
	
	@ManyToMany(mappedBy="phdAdvisors")
	Set<Researcher> phdStudents;
	
	public Set<Researcher> getPhdAdvisors() {
		return phdAdvisors;
	}
	
	public void setPhdAdvisors(Set<Researcher> phdAdvisors) {
		this.phdAdvisors = phdAdvisors;
	}
	
	public Set<Researcher> getPhdStudents() {
		return phdStudents;
	}
	
	public void setPhdStudents(Set<Researcher> phdStudents) {
		this.phdStudents = phdStudents;
	}
	
	
}
