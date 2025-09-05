package com.schedule.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schedule.entity.OrderData;

@Repository
public interface OrderDataRepo extends JpaRepository<OrderData, String> {

}
