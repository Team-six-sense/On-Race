package com.kt.onrace.common.logging.serializer;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.kt.onrace.common.logging.annotation.MaskedField;

public class MaskingIntrospector extends NopAnnotationIntrospector {

	@Override
	public Object findSerializer(Annotated annotated) {
		MaskedField annotation = annotated.getAnnotation(MaskedField.class);
		if (annotation != null) {
			return MaskingSerializer.class;
		}
		return null;
	}
}
