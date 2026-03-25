package com.kt.onrace.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@ConditionalOnClass(name = "jakarta.persistence.EntityManager")
public class JpaAuditingConfig {
}
