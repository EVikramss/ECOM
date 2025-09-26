package com.ecom.services;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.common.Util;
import com.ecom.dto.search.ErrorSearch;
import com.ecom.entity.ServiceError;
import com.ecom.exception.ServiceException;
import com.ecom.repo.ServiceErrorsRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ErrorService {

	@Value("${spring.application.name}")
	private String serviceName;

	@Autowired
	private ServiceErrorsRepo serviceErrorsRepo;

	@Transactional
	public void persistData(ServiceException e) throws JsonProcessingException {

		// get error details
		Object input = e.getInput();
		String exceptionMsg = e.getExceptionMessage();
		StackTraceElement[] stackTraceElements = e.getStackTrace();
		String functionName = e.getFunctionName();

		// get current time
		ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of("UTC"));
		Timestamp date = Timestamp.from(dateTime.toInstant());

		// convert input object to json
		ObjectMapper mapper = new ObjectMapper();
		String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(input);

		// convert stack trace to string
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : stackTraceElements) {
			sb.append(element.toString()).append("\n");
		}
		String stackTrace = sb.toString();

		// limit to 2048 characters
		stackTrace = stackTrace.substring(0, 2048);

		ServiceError error = new ServiceError();
		error.setDate(date);
		error.setErrorKey(Util.generateKey());
		error.setErrorMessage(exceptionMsg);
		error.setFunctionName(functionName);
		error.setService(serviceName);
		error.setInput(prettyJson);
		error.setStackTrace(stackTrace);

		serviceErrorsRepo.save(error);
	}

	public List<String> getServiceNames() {
		return serviceErrorsRepo.getDistinctServiceNames();
	}

	public List<BigInteger> getErrorKeysWithServiceName(ErrorSearch errorSearch, int pageNumber, int pageSize) {
		List<BigInteger> list = serviceErrorsRepo.findErrorKeyByService(errorSearch.getService(), PageRequest
				.of(pageNumber, pageSize, Sort.by(errorSearch.getSortOrder(), errorSearch.getSortByAttribute())));
		return list;
	}

	public ServiceError getErrorDetails(BigInteger errorKey) {
		return serviceErrorsRepo.findById(errorKey).orElseThrow();
	}
}
