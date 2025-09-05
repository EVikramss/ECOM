package com.schedule.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.schedule.entity.OrderStatus;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface OrderStatusRepo extends JpaRepository<OrderStatus, String> {

	List<OrderStatus> findByStatus(int status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("seect os from Orderstatus os where os.orderStatusKey=:id")
	@QueryHints({
		@QueryHint(name = "javax.persistence.lock.timeout", value = "10")
	})
	OrderStatus lockRecord(@Param("id") String orderStatusKey);
}
