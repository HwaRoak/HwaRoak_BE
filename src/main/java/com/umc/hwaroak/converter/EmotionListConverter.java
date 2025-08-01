package com.umc.hwaroak.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.umc.hwaroak.domain.common.Emotion;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.response.ErrorCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.List;

@Converter
public class EmotionListConverter implements AttributeConverter<List<Emotion>, String> {

    private final ObjectMapper objectMapper;

    public EmotionListConverter() {
        this.objectMapper = new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(new JavaTimeModule());
    }

    @Override
    public String convertToDatabaseColumn(List<Emotion> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new GeneralException(ErrorCode.FAILED_EMOTION_PARSING);
        }
    }

    @Override
    public List<Emotion> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<Emotion>>() {
            });
        } catch (IOException e) {
            throw new GeneralException(ErrorCode.FAILED_EMOTION_PARSING);
        }
    }
}
