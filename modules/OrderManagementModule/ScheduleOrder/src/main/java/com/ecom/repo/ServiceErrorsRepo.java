package com.ecom.repo;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.entity.ServiceError;

@Repository
public interface ServiceErrorsRepo extends JpaRepository<ServiceError, BigInteger> {

}
