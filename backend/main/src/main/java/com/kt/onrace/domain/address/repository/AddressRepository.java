package com.kt.onrace.domain.address.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.domain.address.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

	List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

	Optional<Address> findByIdAndUserId(Long id, Long userId);

	boolean existsByUserId(Long userId);

	Optional<Address> findFirstByUserIdAndIsDefaultTrue(Long userId);

	List<Address> findByUserIdOrderByCreatedAtDesc(Long userId);
}
