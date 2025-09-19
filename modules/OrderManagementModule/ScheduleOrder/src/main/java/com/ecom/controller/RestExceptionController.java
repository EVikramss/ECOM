package com.ecom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ecom.exception.ServiceException;
import com.ecom.services.ErrorService;

@ControllerAdvice
public class RestExceptionController {

	@Autowired
	private ErrorService errorService;

	@ExceptionHandler
	public ResponseEntity<String> handleServiceException(ServiceException e) {
		errorService.persistData(e);
		return new ResponseEntity<String>(e.getExceptionMessage(), HttpStatusCode.valueOf(400));
	}

	@ExceptionHandler
	public ResponseEntity<String> handleException(Exception e) {
		errorService.persistData(e);
		return new ResponseEntity<String>(e.getMessage(), HttpStatusCode.valueOf(400));
	}
}
