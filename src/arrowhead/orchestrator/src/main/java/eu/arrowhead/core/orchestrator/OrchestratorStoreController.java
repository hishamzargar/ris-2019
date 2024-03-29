package eu.arrowhead.core.orchestrator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Defaults;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.Utilities.ValidatedPageParams;
import eu.arrowhead.common.dto.OrchestratorStoreListResponseDTO;
import eu.arrowhead.common.dto.OrchestratorStoreModifyPriorityRequestDTO;
import eu.arrowhead.common.dto.OrchestratorStoreRequestDTO;
import eu.arrowhead.common.dto.OrchestratorStoreResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.core.orchestrator.database.service.OrchestratorStoreDBService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin(maxAge = Defaults.CORS_MAX_AGE, allowCredentials = Defaults.CORS_ALLOW_CREDENTIALS, 
			 allowedHeaders = { HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT, HttpHeaders.AUTHORIZATION }
)
@RestController
@RequestMapping(CommonConstants.ORCHESTRATOR_URI)
public class OrchestratorStoreController {

	//=================================================================================================
	// members
	
	private static final String PATH_VARIABLE_ID = "id";
	private static final String ORCHESTRATOR_STORE_MGMT_BY_ID_URI = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/{" + PATH_VARIABLE_ID + "}";
	private static final String ORCHESTRATOR_STORE_MGMT_ALL_TOP_PRIORITY = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_top_priority";
	private static final String ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/all_by_consumer";
	private static final String ORCHESTRATOR_STORE_MGMT_MODIFY = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + "/modify_priorities";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_200_MESSAGE = "OrchestratorStore by requested id returned";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_400_MESSAGE = "No Such OrchestratorStore by requested id";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters returned";
	private static final String GET_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "No Such OrchestratorStore by requested parameters";
	private static final String GET_TOP_PRIORITY_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "Top Priotity OrchestratorStores returned";
	private static final String GET_TOP_PRIORITY_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "Could not find Top Priotity OrchestratorStores";
	private static final String PUT_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters returned";
	private static final String PUT_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "No Such OrchestratorStore by requested parameters";
	private static final String POST_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters created";
	private static final String POST_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "Could not create OrchestratorStore by requested parameters";
	private static final String DELETE_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE = "OrchestratorStore removed";
	private static final String DELETE_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE = "Could not remove OrchestratorStore";
	private static final String POST_ORCHESTRATOR_STORE_MGMT_MODIFY_HTTP_200_MESSAGE = "OrchestratorStores by requested parameters modified";
	private static final String POST_ORCHESTRATOR_STORE_MGMT_MODIFY_HTTP_400_MESSAGE = "Could not modify OrchestratorStore by requested parameters";
	
	private static final String ID_NOT_VALID_ERROR_MESSAGE = "Id must be greater than 0. ";
	private static final String NULL_PARAMETERS_ERROR_MESSAGE = " is null.";
	private static final String EMPTY_PARAMETERS_ERROR_MESSAGE = " is empty.";
	private static final String MODIFY_PRIORITY_MAP_PRIORITY_DUPLICATION_ERROR_MESSAGE = "PriorityMap has duplicated priority value";
	
	private final Logger logger = LogManager.getLogger(OrchestratorStoreController.class);
	
	@Autowired
	private OrchestratorStoreDBService orchestratorStoreDBService;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return an echo message with the purpose of testing the core service availability", response = String.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = CommonConstants.SWAGGER_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI + CommonConstants.ECHO_URI)
	public String echoService() {
		return "Got it!";
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return OrchestratorStore entry by id", response = OrchestratorStoreResponseDTO.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_ORCHESTRATOR_STORE_MGMT_BY_ID_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ORCHESTRATOR_STORE_MGMT_BY_ID_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreResponseDTO getOrchestratorStoreById(@PathVariable(value = PATH_VARIABLE_ID) final long orchestratorStoreId) {		
		logger.debug("getOrchestratorStoreById started ...");
		
		if (orchestratorStoreId < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, ORCHESTRATOR_STORE_MGMT_BY_ID_URI);
		}
		
		return orchestratorStoreDBService.getOrchestratorStoreByIdResponse(orchestratorStoreId);			
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested OrchestratorStore entries by the given parameters", response = OrchestratorStoreListResponseDTO.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreListResponseDTO getOrchestratorStores(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("getOrchestratorStores started ...");
		logger.debug("New OrchestratorStore get request recieved with page: {} and item_per page: {}", page, size);
		
		final ValidatedPageParams vpp = Utilities.validatePageParameters(page, size, direction, CommonConstants.ORCHESTRATOR_URI + CommonConstants.ORCHESTRATOR_STORE_MGMT_URI);
		final OrchestratorStoreListResponseDTO orchestratorStoreListResponse = orchestratorStoreDBService.getOrchestratorStoreEntriesResponse(vpp.getValidatedPage(), vpp.getValidatedSize(), 
																																			  vpp.getValidatedDirecion(), sortField);
		logger.debug("OrchestratorStores  with page: {} and item_per page: {} retrieved successfully", page, size);
		
		return orchestratorStoreListResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested OrchestratorStore entries by the given parameters", response = OrchestratorStoreListResponseDTO.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = GET_TOP_PRIORITY_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = GET_TOP_PRIORITY_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@GetMapping(path = ORCHESTRATOR_STORE_MGMT_ALL_TOP_PRIORITY, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreListResponseDTO getAllTopPriorityOrchestratorStores(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField) {
		logger.debug("getAllTopPriorityOrchestratorStores started ...");
		logger.debug("New OrchestratorStore get request recieved with page: {} and item_per page: {}", page, size);
		
		final ValidatedPageParams vpp = Utilities.validatePageParameters(page, size, direction, CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_TOP_PRIORITY);
		final OrchestratorStoreListResponseDTO orchestratorStoreResponse = orchestratorStoreDBService.getAllTopPriorityOrchestratorStoreEntriesResponse(vpp.getValidatedPage(), vpp.getValidatedSize(),
																																					    vpp.getValidatedDirecion(), sortField);
		logger.debug("OrchestratorStores  with page: {} and item_per page: {} retrieved successfully", page, size);
		
		return orchestratorStoreResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Return requested OrchestratorStore entries specified by the consumer (and the service).", response = OrchestratorStoreListResponseDTO.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = PUT_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = PUT_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreListResponseDTO getOrchestratorStoresByConsumer(
			@RequestParam(name = CommonConstants.REQUEST_PARAM_PAGE, required = false) final Integer page,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_ITEM_PER_PAGE, required = false) final Integer size,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_DIRECTION, defaultValue = Defaults.DEFAULT_REQUEST_PARAM_DIRECTION_VALUE) final String direction,
			@RequestParam(name = CommonConstants.REQUEST_PARAM_SORT_FIELD, defaultValue = CommonConstants.COMMON_FIELD_NAME_ID) final String sortField,
			@RequestBody final OrchestratorStoreRequestDTO request) {
		logger.debug("getOrchestratorStoresByConsumer started ...");
		
		final ValidatedPageParams vpp = Utilities.validatePageParameters(page, size, direction, CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER);
		checkOrchestratorStoreRequestDTOForConsumerIdAndServiceDefinitionName(request, CommonConstants.ORCHESTRATOR_URI + ORCHESTRATOR_STORE_MGMT_ALL_BY_CONSUMER);
		final OrchestratorStoreListResponseDTO orchestratorStoreResponse = orchestratorStoreDBService.getOrchestratorStoresByConsumerResponse(vpp.getValidatedPage(), vpp.getValidatedSize(),
																																			  vpp.getValidatedDirecion(), sortField,
																																			  request.getConsumerSystemId(),
																																			  request.getServiceDefinitionName(),
																																			  request.getServiceInterfaceName());
		
		logger.debug("OrchestratorStores  with ConsumerSystemId : {} and ServiceDefinitionName : {} and  page: {} and item_per page: {} retrieved successfully", request.getConsumerSystemId(),
					 request.getServiceDefinitionName(), page, size);
		return orchestratorStoreResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Create requested OrchestratorStore entries.", response = OrchestratorStoreListResponseDTO.class)
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = CommonConstants.ORCHESTRATOR_STORE_MGMT_URI, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public OrchestratorStoreListResponseDTO addOrchestratorStoreEntries(@RequestBody final List<OrchestratorStoreRequestDTO> request) {
		logger.debug("getOrchestratorStoresByConsumer started ...");
		
		checkOrchestratorStoreRequestDTOList(request, CommonConstants.ORCHESTRATOR_STORE_MGMT_URI );
		final OrchestratorStoreListResponseDTO orchestratorStoreResponse = orchestratorStoreDBService.createOrchestratorStoresResponse(request);
		
		logger.debug(orchestratorStoreResponse.getCount() + " out of " + request.size() + " OrchestratorStore entries created successfully");
		return orchestratorStoreResponse;
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Remove OrchestratorStore")
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = DELETE_ORCHESTRATOR_STORE_MGMT_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = DELETE_ORCHESTRATOR_STORE_MGMT_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@DeleteMapping(path = ORCHESTRATOR_STORE_MGMT_BY_ID_URI)
	public void removeOrchestratorStore(@PathVariable(value = PATH_VARIABLE_ID) final long id) {
		logger.debug("New OrchestratorStore delete request recieved with id: {}", id);
		
		if (id < 1) {
			throw new BadPayloadException(ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, ORCHESTRATOR_STORE_MGMT_BY_ID_URI);
		}
		
		orchestratorStoreDBService.removeOrchestratorStoreById(id);
		logger.debug("OrchestratorStore with id: '{}' successfully deleted", id);
	}
	
	//-------------------------------------------------------------------------------------------------
	@ApiOperation(value = "Modify priorities of OrchestratorStore entries.")
	@ApiResponses(value = {
			@ApiResponse(code = HttpStatus.SC_OK, message = POST_ORCHESTRATOR_STORE_MGMT_MODIFY_HTTP_200_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = POST_ORCHESTRATOR_STORE_MGMT_MODIFY_HTTP_400_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_UNAUTHORIZED, message = CommonConstants.SWAGGER_HTTP_401_MESSAGE),
			@ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = CommonConstants.SWAGGER_HTTP_500_MESSAGE)
	})
	@PostMapping(path = ORCHESTRATOR_STORE_MGMT_MODIFY, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public void modifyPriorities(@RequestBody final OrchestratorStoreModifyPriorityRequestDTO request) {
		logger.debug("modifyPriorities started ...");
		
		checkOrchestratorStoreModifyPriorityRequestDTO(request, ORCHESTRATOR_STORE_MGMT_MODIFY );
		orchestratorStoreDBService.modifyOrchestratorStorePriorityResponse(request);
		
		logger.debug("Priorities modified successfully");
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------	
	private void checkOrchestratorStoreRequestDTOForConsumerIdAndServiceDefinitionName(final OrchestratorStoreRequestDTO request, final String origin) {
		logger.debug("checkOrchestratorStoreRequestDTOForByConsumerRequest started ...");
		
		if (request == null) {
			throw new BadPayloadException("Request "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getConsumerSystemId() == null) {
			throw new BadPayloadException(""+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getConsumerSystemId() < 1) {
			throw new BadPayloadException("ConsumerSystemId : " + ID_NOT_VALID_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getServiceDefinitionName() == null) {
			throw new BadPayloadException("ServiceDefinitionName " + NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(request.getServiceDefinitionName())) {
			throw new BadPayloadException("ServiceDefinitionName : " + EMPTY_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
	
	//-------------------------------------------------------------------------------------------------	
	private void checkOrchestratorStoreRequestDTOList(final List<OrchestratorStoreRequestDTO> request, final String origin) {
		logger.debug("checkOrchestratorStoreRequestDTOList started ...");
		
		if (request == null) {
			throw new BadPayloadException("Request "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if ( request.isEmpty()) {
			throw new BadPayloadException("Request "+ EMPTY_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		for (final OrchestratorStoreRequestDTO orchestratorStoreRequestDTO : request) {
			if (orchestratorStoreRequestDTO == null) {
				throw new BadPayloadException("orchestratorStoreRequestDTO "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (orchestratorStoreRequestDTO.getConsumerSystemId() == null) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ConsumerSystemId "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (orchestratorStoreRequestDTO.getServiceDefinitionName() == null) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ServiceDefinitionId "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			
			if (orchestratorStoreRequestDTO.getProviderSystemDTO() == null) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ProviderSystemDTO "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (orchestratorStoreRequestDTO.getProviderSystemDTO().getAddress() == null) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ProviderSystemDTO.Address "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (Utilities.isEmpty(orchestratorStoreRequestDTO.getProviderSystemDTO().getAddress())) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ProviderSystemDTO.Address "+ EMPTY_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (orchestratorStoreRequestDTO.getProviderSystemDTO().getSystemName() == null) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ProviderSystemDTO.SystemName "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (Utilities.isEmpty(orchestratorStoreRequestDTO.getProviderSystemDTO().getSystemName())) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ProviderSystemDTO.SystemName "+ EMPTY_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (orchestratorStoreRequestDTO.getProviderSystemDTO().getPort() == null) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ProviderSystemDTO.Port "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (orchestratorStoreRequestDTO.getProviderSystemDTO().getPort() < CommonConstants.SYSTEM_PORT_RANGE_MIN || 
				orchestratorStoreRequestDTO.getProviderSystemDTO().getPort() > CommonConstants.SYSTEM_PORT_RANGE_MAX) {
				throw new BadPayloadException("Port must be between " + CommonConstants.SYSTEM_PORT_RANGE_MIN + " and " + CommonConstants.SYSTEM_PORT_RANGE_MAX + ".", HttpStatus.SC_BAD_REQUEST,
											  origin);
			}
			
			if (orchestratorStoreRequestDTO.getServiceInterfaceName() == null) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ServiceInterfaceId "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (Utilities.isEmpty(orchestratorStoreRequestDTO.getServiceInterfaceName())) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.ServiceInterfaceName "+ EMPTY_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (orchestratorStoreRequestDTO.getPriority() == null) {
				throw new BadPayloadException("orchestratorStoreRequestDTO.Priority "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkOrchestratorStoreModifyPriorityRequestDTO(final OrchestratorStoreModifyPriorityRequestDTO request, final String origin) {
		logger.debug("checkOrchestratorStoreModifyPriorityRequestDTO started ...");
		
		if (request == null) {
			throw new BadPayloadException("Request "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getPriorityMap() == null) {
			throw new BadPayloadException("PriorityMap "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (request.getPriorityMap().isEmpty()) {
			throw new BadPayloadException("PriorityMap "+ EMPTY_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final Map<Long, Integer> priorityMap = request.getPriorityMap();
		for (final Entry<Long, Integer> entry : priorityMap.entrySet()) {
			if (entry.getValue() == null) {
				throw new BadPayloadException("PriorityMap.PriorityValue "+ NULL_PARAMETERS_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
			}
			
			if (entry.getValue() < 1) {
				throw new BadPayloadException("PriorityMap.PriorityValue "+  ID_NOT_VALID_ERROR_MESSAGE,HttpStatus.SC_BAD_REQUEST, origin);
			}
		}
		
		final int mapSize = priorityMap.size();
		final Collection<Integer> valuesSet = priorityMap.values();
		if (mapSize != valuesSet.size()) {
			throw new BadPayloadException(MODIFY_PRIORITY_MAP_PRIORITY_DUPLICATION_ERROR_MESSAGE, HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}