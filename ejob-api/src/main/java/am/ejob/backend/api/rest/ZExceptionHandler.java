package am.ejob.backend.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author mukhanov@gmail.com
 */

@ControllerAdvice
public class ZExceptionHandler extends ResponseEntityExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<Object> handle(Exception ex, WebRequest request) throws Exception {

        log.error(ex.getMessage(), ex);

        return handleException(ex, request);
    }
}
