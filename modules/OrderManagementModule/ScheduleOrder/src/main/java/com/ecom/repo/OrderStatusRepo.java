package com.ecom.repo;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecom.entity.OrderStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface OrderStatusRepo extends JpaRepository<OrderStatus, BigInteger> {

	List<OrderStatus> findByStatus(int status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select os from OrderStatus os where os.orderStatusKey=:id")
	@QueryHints({
		@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10")
	})
	OrderStatus lockRecord(@Param("id") BigInteger orderStatusKey);
}
