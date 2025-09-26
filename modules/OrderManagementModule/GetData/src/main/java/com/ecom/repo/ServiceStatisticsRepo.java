package com.ecom.repo;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ecom.entity.ServiceStatistic;

@Repository
public interface ServiceStatisticsRepo extends JpaRepository<ServiceStatistic, BigInteger> {

	@Query("select distinct service from ServiceStatistic")
	List<String> getDistinctServiceNames();

	List<ServiceStatistic> findByService(String service, Pageable pageable);
}