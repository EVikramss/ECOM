package com.ecom.dto.mapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ecom.entity.ServiceError;

@Component
public class ServiceErrorMapper {

	public com.ecom.dto.ServiceError convertToDTO(ServiceError inputData) {
		com.ecom.dto.ServiceError serviceError = new com.ecom.dto.ServiceError();
		serviceError.setDate(inputData.getDate());
		serviceError.setErrorKey(inputData.getErrorKey().toString());
		serviceError.setErrorMessage(inputData.getErrorMessage());
		serviceError.setFunctionName(inputData.getFunctionName());
		serviceError.setInput(inputData.getInput());
		serviceError.setService(inputData.getService());
		serviceError.setStackTrace(inputData.getStackTrace());
		return serviceError;
	}

	public List<com.ecom.dto.ServiceError> convertToDTO(Iterable<ServiceError> inputData) {

		List<com.ecom.dto.ServiceError> output = new ArrayList<com.ecom.dto.ServiceError>();

		Iterator<ServiceError> iter = inputData.iterator();
		while (iter.hasNext()) {
			ServiceError data = iter.next();
			com.ecom.dto.ServiceError outputData = convertToDTO(data);
			output.add(outputData);
		}

		return output;
	}

	public List<String> convertKeysToString(Iterable<BigInteger> inputData) {
		List<String> output = new ArrayList<String>();

		Iterator<BigInteger> iter = inputData.iterator();
		while (iter.hasNext()) {
			BigInteger key = iter.next();
			output.add(key.toString());
		}

		return output;
	}
}
