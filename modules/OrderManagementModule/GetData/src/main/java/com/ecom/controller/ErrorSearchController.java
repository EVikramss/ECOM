package com.ecom.controller;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.dto.mapper.ServiceErrorMapper;
import com.ecom.dto.search.ErrorSearch;
import com.ecom.entity.ServiceError;
import com.ecom.services.ErrorService;

@RestController
public class ErrorSearchController {

	@Autowired
	private ErrorService errorService;

	@Autowired
	private ServiceErrorMapper serviceErrorMapper;

	@PostMapping("/searchError")
	public List<String> searchError(@RequestBody ErrorSearch searchParams, @RequestParam int pageNumber,
			@RequestParam int pageSize) {
		Iterable<BigInteger> data = errorService.getErrorKeysWithServiceName(searchParams, pageNumber, pageSize);
		return serviceErrorMapper.convertKeysToString(data);
	}

	@GetMapping("/getError")
	public com.ecom.dto.ServiceError getError(@RequestParam BigInteger errorKey) {
		ServiceError data = errorService.getErrorDetails(errorKey);
		return serviceErrorMapper.convertToDTO(data);
	}

	@GetMapping("/getErrorServiceNames")
	public List<String> getErrorServiceNames() {
		return errorService.getServiceNames();
	}
}
