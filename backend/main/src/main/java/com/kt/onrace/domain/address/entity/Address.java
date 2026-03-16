package com.kt.onrace.domain.address.entity;

import com.kt.onrace.common.entity.BaseEntity;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "address",
	indexes = {
		@Index(name = "idx_address_user", columnList = "user_id"),
		@Index(name = "idx_address_user_default", columnList = "user_id,is_default")
	}
)
public class Address extends BaseEntity {

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(length = 20)
	private String label;

	@Column(nullable = false, length = 50)
	private String receiverName;

	@Column(nullable = false, length = 20)
	private String phone;

	@Column(nullable = false, length = 10)
	private String zipcode;

	@Column(nullable = false, length = 255)
	private String address1;

	@Column(length = 255)
	private String address2;

	@Column(length = 255)
	private String memo;

	@Column(name = "is_default", nullable = false)
	private boolean isDefault;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder
	private Address(Long userId, String label, String receiverName, String phone, String zipcode,
					String address1, String address2, String memo, Boolean isDefault,
					Boolean isDeleted, LocalDateTime deletedAt) {
		this.userId = userId;
		this.label = label;
		this.receiverName = receiverName;
		this.phone = phone;
		this.zipcode = zipcode;
		this.address1 = address1;
		this.address2 = address2;
		this.memo = memo;
		this.isDefault = isDefault != null && isDefault;
		this.isDeleted = isDeleted != null && isDeleted;
		this.deletedAt = deletedAt;
	}

	public void update(String label, String receiverName, String phone, String zipcode,
					String address1, String address2, String memo) {
		this.label = label;
		this.receiverName = receiverName;
		this.phone = phone;
		this.zipcode = zipcode;
		this.address1 = address1;
		this.address2 = address2;
		this.memo = memo;
	}

	public void markDefault() {
		this.isDefault = true;
	}

	public void unmarkDefault() {
		this.isDefault = false;
	}

	public void softDelete() {
		this.isDeleted = true;
		this.deletedAt = LocalDateTime.now();
		this.isDefault = false;
	}
}
