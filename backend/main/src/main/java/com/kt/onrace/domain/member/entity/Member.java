package com.kt.onrace.domain.member.entity;

import com.kt.onrace.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    private Long id;

    @Column(nullable = false)
    private boolean isDeleted;

    @Builder
    private Member(Long id, boolean isDeleted) {
        this.id = id;
        this.isDeleted = isDeleted;
    }

    public static Member createMember(Long userId) {
        return Member.builder()
                .id(userId)
                .isDeleted(false)
                .build();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }

}
