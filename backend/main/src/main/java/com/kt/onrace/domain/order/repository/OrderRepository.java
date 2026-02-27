package com.kt.onrace.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kt.onrace.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}