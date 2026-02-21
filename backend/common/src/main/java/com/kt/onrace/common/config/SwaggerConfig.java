package com.kt.onrace.common.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(apiInfo())
			.servers(servers())
			.components(securityComponents())
			.addSecurityItem(securityRequirement());
	}

	private Info apiInfo() {
		return new Info()
			.title("On-Race (Traffic Master) API")
			.description("""
				대용량 티켓팅 플랫폼 API 문서
				
				개발팀 구성
				- 풀스택: 이세현(팀장), 최우수
				- 백엔드: 정종한, 양정요
				- 프론트엔드: 이현수, 이주형
				
				기술 스택
				- Backend: ?
				- Database: ?
				""")
			.version("v1.0.0");
	}

	private List<Server> servers() {
		return List.of(
			new Server()
				.url("http://localhost:8080")
				.description("로컬 개발 서버")
			/*new Server()
				.url("https://dev-api.onrace.com")
				.description("개발 서버 (DEV)")*/
		);
	}

	private Components securityComponents() {
		return new Components()
			.addSecuritySchemes("bearerAuth",
				new SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")
					.description("JWT 토큰 입력 (Bearer 제외)")
			);
	}

	private SecurityRequirement securityRequirement() {
		return new SecurityRequirement().addList("bearerAuth");
	}
}
