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
	uniqueConstraints = {
		@jakarta.persistence.UniqueConstraint(name = "uk_address_user_normalized_label", columnNames = {"user_id", "normalized_label"}),
		@jakarta.persistence.UniqueConstraint(name = "uk_address_active_default_owner", columnNames = {"active_default_owner_id"})
	},
	indexes = {
		@Index(name = "idx_address_user", columnList = "user_id"),
		@Index(name = "idx_address_user_deleted", columnList = "user_id,is_deleted"),
		@Index(name = "idx_address_user_deleted_default", columnList = "user_id,is_deleted,is_default")
	}
)
public class Address extends BaseEntity {

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(length = 20)
	private String label;

	@Column(name = "normalized_label", length = 20)
	private String normalizedLabel;

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

	@Column(name = "active_default_owner_id")
	private Long activeDefaultOwnerId;

	@Builder
	private Address(Long userId, String label, String normalizedLabel, String receiverName, String phone, String zipcode,
					String address1, String address2, String memo, Boolean isDefault,
					Boolean isDeleted, LocalDateTime deletedAt, Long activeDefaultOwnerId) {
		this.userId = userId;
		this.label = label;
		this.normalizedLabel = normalizedLabel;
		this.receiverName = receiverName;
		this.phone = phone;
		this.zipcode = zipcode;
		this.address1 = address1;
		this.address2 = address2;
		this.memo = memo;
		this.isDefault = isDefault != null && isDefault;
		this.isDeleted = isDeleted != null && isDeleted;
		this.deletedAt = deletedAt;
		this.activeDefaultOwnerId = activeDefaultOwnerId;
	}

	public void update(String label, String normalizedLabel, String receiverName, String phone, String zipcode,
					String address1, String address2, String memo) {
		this.label = label;
		this.normalizedLabel = normalizedLabel;
		this.receiverName = receiverName;
		this.phone = phone;
		this.zipcode = zipcode;
		this.address1 = address1;
		this.address2 = address2;
		this.memo = memo;
	}

	public void markDefault() {
		this.isDefault = true;
		this.activeDefaultOwnerId = this.userId;
	}

	public void unmarkDefault() {
		this.isDefault = false;
		this.activeDefaultOwnerId = null;
	}

	public void softDelete() {
		this.isDeleted = true;
		this.deletedAt = LocalDateTime.now();
		this.isDefault = false;
		this.activeDefaultOwnerId = null;
		this.normalizedLabel = null;
	}
}
