package com.cht.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.cht.demo.ServiceException;


public class JsonUtils {
	static final ObjectMapper JACKSON = new ObjectMapper();
	
	static {
		JACKSON.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		JACKSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JACKSON.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		
		JACKSON.registerModule(new JavaTimeModule());
		JACKSON.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		
	}
	
	public static final String getField(String json, String field) {
		try {
			JsonNode node = JACKSON.readTree(json);
			node = node.get(field);
			if (node != null) {
				return node.asText();
			}
			
			return null;
			
		} catch (Exception e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}
	
	public static <T> T fromJson(InputStream is, Class<T> clazz) {
		try {
			return JACKSON.readValue(is, clazz);
			
		} catch (IOException e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}
	
	public static final <T> T fromJson(Reader r, Class<T> clazz) throws IOException {
		return JACKSON.readValue(r, clazz);
	}
	
	public static final <T> T fromJson(Reader r, TypeReference<T> ref) throws IOException {
		return JACKSON.readValue(r, ref);
	}
	
	public static final <T> T fromJson(String json, TypeReference<T> ref) {
		try {
			return JACKSON.readValue(json, ref);
			
		} catch (Exception e) {
			String error = String.format("%s - %s", json, e.getMessage());			
			throw new ServiceException(error, e);
		}
	}
	
	public static final <T> T fromJson(String json, Class<T> clazz) {
		try {
			StringReader sr = new StringReader(json);		
			return fromJson(sr, clazz);
			
		} catch (Exception e) {
			String error = String.format("%s - %s", json, e.getMessage());			
			throw new ServiceException(error, e);
		}
	}
	
	public static final void toJson(Writer w, Object value) throws IOException {
		JACKSON.writeValue(w, value);
	}

	public static final String toJson(Object value) {
		try {
			StringWriter sw = new StringWriter();
			toJson(sw, value);		
			return sw.toString();
			
		} catch (Exception e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}	
	
	public static final String toPrettyPrintJson(Object value) {
		try {
			StringWriter sw = new StringWriter();
			JACKSON.writerWithDefaultPrettyPrinter().writeValue(sw, value);		
			return sw.toString();
			
		} catch (Exception e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}
	
	public static final void toPrettyPrintJson(Writer w, Object value) {
		try {
			JACKSON.writerWithDefaultPrettyPrinter().writeValue(w, value);
			
		} catch (Exception e) {
			throw new ServiceException(e.getMessage(), e);
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> toMap(Object value) {
		return JACKSON.convertValue(value, Map.class);
	}
	
	public static final <T> T fromMap(Map<?, ?> map, Class<T> clazz) {
		return JACKSON.convertValue(map, clazz);
	}
}
