package eu.arrowhead.common.database.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import eu.arrowhead.common.database.entity.AuthorizationIntraCloud;
import eu.arrowhead.common.database.entity.ServiceDefinition;
import eu.arrowhead.common.database.entity.System;

@Repository
public interface AuthorizationIntraCloudRepository extends RefreshableRepository<AuthorizationIntraCloud,Long> {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Query("SELECT entry FROM AuthorizationIntraCloud entry WHERE consumerSystem.id = ?1 AND providerSystem.id = ?2 AND serviceDefinition.id = ?3")
	public Optional<AuthorizationIntraCloud> findByConsumerIdAndProviderIdAndServiceDefinitionId(final long consumerId, final long providerId, final long serviceDefinitionId);
	
	//-------------------------------------------------------------------------------------------------
	public Optional<AuthorizationIntraCloud> findByConsumerSystemAndProviderSystemAndServiceDefinition(final System consumer, final System provider, final ServiceDefinition serviceDefinition);
}