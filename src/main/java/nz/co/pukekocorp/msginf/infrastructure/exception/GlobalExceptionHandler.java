/*
 * Copyright (c) 2024. Pukeko Corporation Ltd
 * All rights reserved.
 */

package nz.co.pukekocorp.msginf.infrastructure.exception;

import nz.co.pukekocorp.msginf.models.error.ValidationErrors;
import nz.co.pukekocorp.msginf.models.user.UserResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        ValidationErrors validationErrors = getValidationErrors(ex);
        // Return different response entities based on url
        String url = request.getDescription(false);
        if (url.contains("message")) {
            // RestMessageResponse
            return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
        } else if (url.contains("user")) {
            // UserResponse
            UserResponse response = new UserResponse();
            response.setValidationErrors(validationErrors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else if (url.contains("auth")) {
            // JwtError
            return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
        }
    }

    private ValidationErrors getValidationErrors(MethodArgumentNotValidException ex) {
        ValidationErrors validationErrors = new ValidationErrors();
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
        validationErrors.setErrors(errors);
        return validationErrors;
    }
}
