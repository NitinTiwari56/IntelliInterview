package com.interviewiq.aptitude.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/** List<String> (options) ko DB mein JSON text ki tarah store karta hai: ["a","b","c","d"] */
@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        try {
            return MAPPER.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize options", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not read options", e);
        }
    }
}
