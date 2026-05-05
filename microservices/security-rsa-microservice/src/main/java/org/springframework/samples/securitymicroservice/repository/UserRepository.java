package org.springframework.samples.securitymicroservice.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.samples.securitymicroservice.model.User;


public interface UserRepository extends  CrudRepository<User, String>{
	User findByUsername(String username);
}
