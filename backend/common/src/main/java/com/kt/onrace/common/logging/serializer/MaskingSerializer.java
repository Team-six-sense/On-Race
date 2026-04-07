package com.kt.onrace.common.logging.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.kt.onrace.common.logging.annotation.MaskedField;
import com.kt.onrace.common.util.MaskingType;
import com.kt.onrace.common.util.MaskingUtils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class MaskingSerializer extends JsonSerializer<Object> implements ContextualSerializer {

	private MaskingType maskingType;

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}
		gen.writeString(MaskingUtils.mask(String.valueOf(value), maskingType));
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
		throws JsonMappingException {

		if (property == null) {
			return this;
		}

		MaskedField annotation = property.getAnnotation(MaskedField.class);
		if (annotation == null) {
			annotation = property.getContextAnnotation(MaskedField.class);
		}

		if (annotation != null) {
			return new MaskingSerializer(annotation.value());
		}

		return this;
	}
}
