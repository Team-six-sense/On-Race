package com.kt.onrace.common.logging.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.kt.onrace.common.logging.serializer.MaskingIntrospector;

@Configuration
public class LogMapperConfig {

	@Bean
	@Primary
	public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
		return builder.createXmlMapper(false).build();
	}

	@Bean
	@Qualifier("loggingObjectMapper")
	public ObjectMapper loggingObjectMapper(Jackson2ObjectMapperBuilder builder) {
		ObjectMapper mapper = builder.createXmlMapper(false).build();

		AnnotationIntrospector existing = mapper.getSerializationConfig().getAnnotationIntrospector();
		AnnotationIntrospector maskingIntrospector = new MaskingIntrospector();
		AnnotationIntrospector pair = new AnnotationIntrospectorPair(maskingIntrospector, existing);

		mapper.setAnnotationIntrospector(pair);

		return mapper;
	}
}
