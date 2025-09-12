package com.ecom.repo;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.entity.ServiceStatistic;

@Repository
public interface ServiceStatisticsRepo extends JpaRepository<ServiceStatistic, BigInteger> {

}