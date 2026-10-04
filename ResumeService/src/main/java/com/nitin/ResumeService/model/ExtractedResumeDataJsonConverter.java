package com.nitin.ResumeService.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nitin.ResumeService.dto.extracted.ExtractedResumeData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ExtractedResumeDataJsonConverter implements AttributeConverter<ExtractedResumeData, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ExtractedResumeData attribute) {
        if (attribute == null) {
            return "{}";
        }
        try {
            return mapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    @Override
    public ExtractedResumeData convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || dbData.equals("{}")) {
            return new ExtractedResumeData();
        }
        try {
            return mapper.readValue(dbData, ExtractedResumeData.class);
        } catch (JsonProcessingException e) {
            return new ExtractedResumeData();
        }
    }
}
