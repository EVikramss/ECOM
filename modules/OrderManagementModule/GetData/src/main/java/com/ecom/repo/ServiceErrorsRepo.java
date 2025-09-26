package com.ecom.repo;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecom.entity.ServiceError;

@Repository
public interface ServiceErrorsRepo extends JpaRepository<ServiceError, BigInteger> {
	
	@Query("select distinct service from ServiceError")
	List<String> getDistinctServiceNames();

	@Query("select errorKey from ServiceError where service=:svc")
	List<BigInteger> findErrorKeyByService(@Param("svc") String service, Pageable pageable);
}
