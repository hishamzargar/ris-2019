package eu.arrowhead.common.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;

@ControllerAdvice
public class ArrowheadResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	//=================================================================================================
	// members
	
	private Logger log = LogManager.getLogger(ArrowheadResponseEntityExceptionHandler.class);
	private static final HttpHeaders headers = new HttpHeaders();
	static {
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
	}

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@ExceptionHandler(ArrowheadException.class)
	public ResponseEntity<Object> handleArrowheadException(final ArrowheadException ex, final WebRequest request) {
		final String origin = ex.getOrigin() != null ? ex.getOrigin() : request.getContextPath();
		final HttpStatus status = Utilities.calculateHttpStatusFromArrowheadException(ex);
		log.debug("{} at {}: {}", ex.getClass().getName(), origin, ex.getMessage());
		log.debug("Exception", ex);
		final ErrorMessageDTO dto = new ErrorMessageDTO(ex);
		if (ex.getErrorCode() <= 0) {
			dto.setErrorCode(status.value());
		}
		
		return handleExceptionInternal(ex, dto, headers, status, request);
	}
}