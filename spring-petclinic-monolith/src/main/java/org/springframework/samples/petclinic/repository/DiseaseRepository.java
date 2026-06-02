package org.springframework.samples.petclinic.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.samples.petclinic.model.Disease;


@RepositoryRestResource(path = "diseases", collectionResourceRel = "diseases")
public interface DiseaseRepository extends  CrudRepository<Disease, Integer>{

	@RestResource(exported = true)
	Collection<Disease> findAll();

	@RestResource(exported = true)
	Optional<Disease> findById(Integer id);

	@RestResource(exported = true)
	<S extends Disease> S save(S disease);

	@RestResource(exported = true)
	void deleteById(Integer id);

	@RestResource(path = "by-name", rel = "by-name")
	Collection<Disease> findByNameContainingIgnoreCase(@Param("name") String name);
}
