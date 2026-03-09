package com.kt.onrace.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.auth.entity.EmailSend;

public interface EmailSendRepository extends JpaRepository<EmailSend, Long> {
}
