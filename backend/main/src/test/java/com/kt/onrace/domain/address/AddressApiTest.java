package com.kt.onrace.domain.address;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.onrace.common.config.JpaAuditingConfig;
import com.kt.onrace.common.config.QueryDslConfig;
import com.kt.onrace.common.exception.GlobalExceptionHandler;
import com.kt.onrace.domain.address.controller.AddressController;
import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.address.service.AddressService;

@DataJpaTest
@Import({AddressService.class, JpaAuditingConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
class AddressApiTest {

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private AddressService addressService;

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();

		mockMvc = MockMvcBuilders.standaloneSetup(new AddressController(addressService))
			.setControllerAdvice(new GlobalExceptionHandler())
			.setValidator(validator)
			.build();
	}

	@Test
	@DisplayName("배송지 생성 후 목록 조회 시 자동 라벨과 기본배송지 설정이 반영된다")
	void createAndListAddress() throws Exception {
		mockMvc.perform(post("/addresses")
				.header("X-User-Id", 1L)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest("홍길동", null, false))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.label").value("배송지1"))
			.andExpect(jsonPath("$.data.phone").value("01011112222"))
			.andExpect(jsonPath("$.data.isDefault").value(true));

		mockMvc.perform(get("/addresses")
				.header("X-User-Id", 1L))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data[0].receiverName").value("홍길동"))
			.andExpect(jsonPath("$.data[0].label").value("배송지1"))
			.andExpect(jsonPath("$.data[0].phone").value("01011112222"))
			.andExpect(jsonPath("$.data[0].isDefault").value(true));
	}

	@Test
	@DisplayName("배송지 생성 시 trim 및 대소문자 무시 기준으로 라벨 중복을 검사한다")
	void createRejectsDuplicateLabel() throws Exception {
		mockMvc.perform(post("/addresses")
				.header("X-User-Id", 1L)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest("홍길동", "HOME", false))))
			.andExpect(status().isOk());

		mockMvc.perform(post("/addresses")
				.header("X-User-Id", 1L)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest("김철수", " home ", false))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value("ADR_002"))
			.andExpect(jsonPath("$.message").value("이미 사용 중인 주소 별칭입니다."));
	}

	@Test
	@DisplayName("배송지 수정 시 빈 라벨을 보내면 기존 라벨을 유지한다")
	void updateKeepsLabelWhenBlank() throws Exception {
		String response = mockMvc.perform(post("/addresses")
				.header("X-User-Id", 1L)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest("홍길동", "집", false))))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();

		Long addressId = objectMapper.readTree(response).path("data").path("id").asLong();

		mockMvc.perform(put("/addresses/{id}", addressId)
				.header("X-User-Id", 1L)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest("홍길동2", "", null))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.receiverName").value("홍길동2"))
			.andExpect(jsonPath("$.data.label").value("집"));
	}

	@Test
	@DisplayName("전화번호 길이가 10자리 또는 11자리가 아니면 등록을 거부한다")
	void createRejectsInvalidPhoneLength() throws Exception {
		mockMvc.perform(post("/addresses")
				.header("X-User-Id", 1L)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest("홍길동", "집", false, "010123456789"))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value("ADR_004"))
			.andExpect(jsonPath("$.message").value("전화번호는 하이픈을 제외하고 10자리 또는 11자리여야 합니다."));
	}

	@Test
	@DisplayName("사용자당 배송지가 10개를 초과하면 등록을 거부한다")
	void createRejectsWhenAddressLimitExceeded() throws Exception {
		for (int i = 1; i <= 10; i++) {
			mockMvc.perform(post("/addresses")
					.header("X-User-Id", 1L)
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(createRequest("홍길동" + i, "라벨" + i, false))))
				.andExpect(status().isOk());
		}

		mockMvc.perform(post("/addresses")
				.header("X-User-Id", 1L)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest("홍길동11", "라벨11", false))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value("ADR_003"))
			.andExpect(jsonPath("$.message").value("배송지는 최대 10개까지 등록할 수 있습니다."));
	}

	private AddressDto.SaveRequest createRequest(String receiverName, String label, Boolean isDefault) {
		return createRequest(receiverName, label, isDefault, "010-1111-2222");
	}

	private AddressDto.SaveRequest createRequest(String receiverName, String label, Boolean isDefault, String phone) {
		return new AddressDto.SaveRequest(
			receiverName,
			label,
			phone,
			"12345",
			"서울",
			"101동",
			"문앞",
			isDefault
		);
	}
}
