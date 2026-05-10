package org.springframework.samples.securityoauth2microservice.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.samples.securityoauth2microservice.model.ExternalIdentity;

public interface ExternalIdentityRepository extends CrudRepository<ExternalIdentity, Integer> {

	Optional<ExternalIdentity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
