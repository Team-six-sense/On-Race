package com.kt.onrace.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kt.onrace.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
