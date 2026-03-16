package com.kt.onrace.domain.address.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kt.onrace.domain.address.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

	@Query("""
		select a
		from Address a
		where a.userId = :userId
			and a.isDeleted = false
		order by a.isDefault desc, a.createdAt desc
		""")
	List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(@Param("userId") Long userId);

	@Query("""
		select a
		from Address a
		where a.id = :id
			and a.userId = :userId
			and a.isDeleted = false
		""")
	Optional<Address> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	@Query("""
		select count(a) > 0
		from Address a
		where a.userId = :userId
			and a.isDeleted = false
		""")
	boolean existsByUserId(@Param("userId") Long userId);

	@Query("""
		select count(a)
		from Address a
		where a.userId = :userId
			and a.isDeleted = false
		""")
	long countByUserId(@Param("userId") Long userId);

	@Query("""
		select a
		from Address a
		where a.userId = :userId
			and a.isDefault = true
			and a.isDeleted = false
			and a.activeDefaultOwnerId = :userId
		""")
	Optional<Address> findFirstByUserIdAndIsDefaultTrue(@Param("userId") Long userId);

	@Query("""
		select a
		from Address a
		where a.userId = :userId
			and a.isDeleted = false
		order by a.createdAt desc
		""")
	List<Address> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

	@Query("""
		select a.label
		from Address a
		where a.userId = :userId
			and a.label is not null
			and a.isDeleted = false
		""")
	List<String> findLabelsByUserId(@Param("userId") Long userId);

	long countByUserIdAndIsDeletedFalseAndNormalizedLabel(Long userId, String normalizedLabel);

	long countByUserIdAndIdNotAndIsDeletedFalseAndNormalizedLabel(Long userId, Long addressId, String normalizedLabel);
}
