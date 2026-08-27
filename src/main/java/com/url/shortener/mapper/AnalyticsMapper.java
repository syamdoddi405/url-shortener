package com.url.shortener.mapper;

import org.springframework.stereotype.Component;
import com.url.shortener.dto.AnalyticsDTO;
import com.url.shortener.entity.AnalyticsEntity;

/**
 * Mapper for converting between AnalyticsEntity and AnalyticsDTO.
 * Follows the Mapper/Converter design pattern to handle entity-DTO transformations.
 * Ensures separation of concerns and reduces boilerplate code.
 */
@Component
public class AnalyticsMapper {

    /**
     * Converts AnalyticsEntity to AnalyticsDTO.
     *
     * @param entity the JPA entity
     * @return the data transfer object
     */
    public AnalyticsEntity toDTO(AnalyticsDTO entity) {  
        if (entity == null) {
            return null;
        }
        return AnalyticsEntity.builder()
                .id(entity.getId())
                .shortCode(entity.getShortCode())
                .originalUrl(entity.getOriginalUrl())
                .totalClicks(entity.getTotalClicks())
                .createdAt(entity.getCreatedAt())
                .lastAccessed(entity.getLastAccessed())
                .lastReferrer(entity.getLastReferrer())
                .lastUserAgent(entity.getLastUserAgent())
                .build();
    }

    /**
     * Converts AnalyticsDTO to AnalyticsEntity.
     *
     * @param dto the data transfer object
     * @return the JPA entity
     */
    public AnalyticsEntity toEntity(AnalyticsDTO dto) {
        if (dto == null) {
            return null;
        }
        return AnalyticsEntity.builder()
                .id(dto.getId())
                .shortCode(dto.getShortCode())
                .originalUrl(dto.getOriginalUrl())
                .totalClicks(dto.getTotalClicks())
                .createdAt(dto.getCreatedAt())
                .lastAccessed(dto.getLastAccessed())
                .lastReferrer(dto.getLastReferrer())
                .lastUserAgent(dto.getLastUserAgent())
                .build();
    }
}
