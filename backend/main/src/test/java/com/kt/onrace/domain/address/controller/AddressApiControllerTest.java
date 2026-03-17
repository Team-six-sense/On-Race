package com.kt.onrace.domain.address.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.GlobalExceptionHandler;
import com.kt.onrace.common.filter.GatewayAccessFilter;
import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.address.service.AddressService;

@WebMvcTest(
	controllers = {AddressController.class, AddressCompatController.class},
	excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = GatewayAccessFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AddressApiControllerTest {

	private static final String USER_ID_HEADER = "X-User-Id";
	private static final Long USER_ID = 7L;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AddressService addressService;

	private static Stream<Arguments> routeSpecs() {
		return Stream.of(
			Arguments.of("/addresses", "/addresses/{id}/default"),
			Arguments.of("/address", "/address/{id}")
		);
	}

	@ParameterizedTest
	@MethodSource("routeSpecs")
	void listReturnsAddressItems(String basePath, String defaultPath) throws Exception {
		given(addressService.list(USER_ID)).willReturn(List.of(defaultAddress(), latestAddress()));

		mockMvc.perform(get(basePath).header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data[0].id").value(1L))
			.andExpect(jsonPath("$.data[0].label").value("집"))
			.andExpect(jsonPath("$.data[0].isDefault").value(true))
			.andExpect(jsonPath("$.data[1].id").value(2L))
			.andExpect(jsonPath("$.data[1].label").value("회사"))
			.andExpect(jsonPath("$.data[1].isDefault").value(false));

		verify(addressService).list(USER_ID);
	}

	@ParameterizedTest
	@MethodSource("routeSpecs")
	void getDefaultReturnsEmptyState(String basePath, String defaultPath) throws Exception {
		given(addressService.getDefault(USER_ID)).willReturn(AddressDto.DefaultResponse.empty());

		mockMvc.perform(get(basePath + "/default").header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.hasAddress").value(false))
			.andExpect(jsonPath("$.data.address").isEmpty());

		verify(addressService).getDefault(USER_ID);
	}

	@ParameterizedTest
	@MethodSource("routeSpecs")
	void getReturnsBusinessErrorWhenAddressDoesNotExist(String basePath, String defaultPath) throws Exception {
		given(addressService.get(USER_ID, 99L)).willThrow(new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));

		mockMvc.perform(get(basePath + "/{id}", 99L).header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value(BusinessErrorCode.ADDRESS_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(BusinessErrorCode.ADDRESS_NOT_FOUND.getMessage()));

		verify(addressService).get(USER_ID, 99L);
	}

	@ParameterizedTest
	@MethodSource("routeSpecs")
	void createReturnsCreatedAddress(String basePath, String defaultPath) throws Exception {
		given(addressService.create(eq(USER_ID), any(AddressDto.SaveRequest.class))).willReturn(defaultAddress());

		mockMvc.perform(post(basePath)
				.header(USER_ID_HEADER, USER_ID)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(saveRequest())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.id").value(1L))
			.andExpect(jsonPath("$.data.label").value("집"))
			.andExpect(jsonPath("$.data.receiverName").value("홍길동"));

		verify(addressService).create(eq(USER_ID), any(AddressDto.SaveRequest.class));
	}

	@ParameterizedTest
	@MethodSource("routeSpecs")
	void createRejectsBlankReceiverName(String basePath, String defaultPath) throws Exception {
		AddressDto.SaveRequest invalidRequest = new AddressDto.SaveRequest(
			"",
			"01012345678",
			"12345",
			"서울시 강남구",
			"101동",
			"문앞",
			true,
			"집"
		);

		mockMvc.perform(post(basePath)
				.header(USER_ID_HEADER, USER_ID)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value(BusinessErrorCode.COMMON_INVALID_PARAMETER.getCode()));

		verifyNoInteractions(addressService);
	}

	@ParameterizedTest
	@MethodSource("routeSpecs")
	void updateReturnsUpdatedAddress(String basePath, String defaultPath) throws Exception {
		given(addressService.update(eq(USER_ID), eq(2L), any(AddressDto.SaveRequest.class))).willReturn(latestAddress());

		mockMvc.perform(put(basePath + "/{id}", 2L)
				.header(USER_ID_HEADER, USER_ID)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(saveRequest())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.data.id").value(2L))
			.andExpect(jsonPath("$.data.label").value("회사"));

		verify(addressService).update(eq(USER_ID), eq(2L), any(AddressDto.SaveRequest.class));
	}

	@ParameterizedTest
	@MethodSource("routeSpecs")
	void deleteReturnsSuccess(String basePath, String defaultPath) throws Exception {
		doNothing().when(addressService).delete(USER_ID, 2L);

		mockMvc.perform(delete(basePath + "/{id}", 2L).header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.message").value("요청이 성공했습니다"))
			.andExpect(jsonPath("$.data").doesNotExist());

		verify(addressService).delete(USER_ID, 2L);
	}

	@ParameterizedTest
	@MethodSource("routeSpecs")
	void setDefaultReturnsSuccess(String basePath, String defaultPath) throws Exception {
		doNothing().when(addressService).setDefault(USER_ID, 2L);

		mockMvc.perform(patch(defaultPath, 2L).header(USER_ID_HEADER, USER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.code").value("SUCCESS"))
			.andExpect(jsonPath("$.message").value("요청이 성공했습니다"))
			.andExpect(jsonPath("$.data").doesNotExist());

		verify(addressService).setDefault(USER_ID, 2L);
	}

	@ParameterizedTest
	@MethodSource("routeSpecs")
	void missingUserIdHeaderReturnsInvalidParameter(String basePath, String defaultPath) throws Exception {
		mockMvc.perform(get(basePath))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value(BusinessErrorCode.COMMON_INVALID_PARAMETER.getCode()))
			.andExpect(jsonPath("$.message").value(BusinessErrorCode.COMMON_INVALID_PARAMETER.getMessage()));

		verifyNoInteractions(addressService);
	}

	private AddressDto.SaveRequest saveRequest() {
		return new AddressDto.SaveRequest(
			"홍길동",
			"01012345678",
			"12345",
			"서울시 강남구",
			"101동",
			"문앞",
			true,
			"집"
		);
	}

	private static AddressDto.Response defaultAddress() {
		return new AddressDto.Response(
			1L,
			"집",
			"홍길동",
			"01012345678",
			"12345",
			"서울시 강남구",
			"101동",
			"문앞",
			true
		);
	}

	private static AddressDto.Response latestAddress() {
		return new AddressDto.Response(
			2L,
			"회사",
			"홍길동",
			"01099998888",
			"54321",
			"서울시 서초구",
			"202동",
			"경비실",
			false
		);
	}
}
