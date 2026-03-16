package com.kt.onrace.domain.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserIdAndOrderStatusOrderByCreatedAtDesc(Long userId, OrderStatus orderStatus);

	Optional<Order> findByOrderNumberAndUserId(String orderNumber, Long userId);
}
