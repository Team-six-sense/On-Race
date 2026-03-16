package com.kt.onrace.domain.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.domain.order.entity.OrderPackage;

public interface OrderPackageRepository extends JpaRepository<OrderPackage, Long> {

	List<OrderPackage> findByOrderIdOrderByIdAsc(Long orderId);
}
