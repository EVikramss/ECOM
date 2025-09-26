package com.ecom.repo;

import java.math.BigInteger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.ecom.entity.OrderData;

@Repository
public interface OrderDataRepo extends JpaRepository<OrderData, BigInteger>, QuerydslPredicateExecutor<OrderData> {
	OrderData findByOrderNo(String orderNo);
}
