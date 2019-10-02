package eu.arrowhead.core.orchestrator.matchmaking;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import eu.arrowhead.common.dto.DTOUtilities;
import eu.arrowhead.common.dto.PreferredProviderDataDTO;
import eu.arrowhead.common.dto.ServiceRegistryResponseDTO;

public class SecurityFilteredIntraCloudProviderMatchmaker implements IntraCloudProviderMatchmakingAlgorithm {
	
	//=================================================================================================
	// members
	
	private static final Logger logger = LogManager.getLogger(SecurityFilteredIntraCloudProviderMatchmaker.class);

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	/** 
	 * TODO
	 */
	@Override
	public ServiceRegistryResponseDTO doMatchmaking(final List<ServiceRegistryResponseDTO> srList, final IntraCloudProviderMatchmakingParameters params) {
		//TODO
		
		return null;
	}
}