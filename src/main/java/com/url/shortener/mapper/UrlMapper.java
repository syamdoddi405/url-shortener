package com.url.shortener.mapper;

import org.springframework.stereotype.Component;
import com.url.shortener.dto.UrlDTO;
import com.url.shortener.entity.UrlEntity;

/**
 * Mapper for converting between UrlEntity and UrlDTO.
 * Follows the Mapper/Converter design pattern to handle entity-DTO transformations.
 * Ensures separation of concerns and reduces boilerplate code.
 */
@Component
public class UrlMapper {

    /**
     * Converts UrlEntity to UrlDTO.
     *
     * @param dto the data transfer object
     * @return the JPA entity
     */
    public UrlEntity toEntity(UrlDTO dto) {
        if (dto == null) {
            return null;
        }
        return UrlEntity.builder()
                .id(dto.getId())
                .originalUrl(dto.getOriginalUrl())
                .shortCode(dto.getShortCode())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
