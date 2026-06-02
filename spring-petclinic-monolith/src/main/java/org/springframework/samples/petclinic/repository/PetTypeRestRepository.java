package org.springframework.samples.petclinic.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.samples.petclinic.model.PetType;

@RepositoryRestResource(path = "pet-types", collectionResourceRel = "petTypes")
public interface PetTypeRestRepository extends Repository<PetType, Integer> {

	@RestResource(exported = true)
	Collection<PetType> findAll();

	@RestResource(exported = true)
	Optional<PetType> findById(Integer id);

}
