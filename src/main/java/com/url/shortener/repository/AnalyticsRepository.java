package com.url.shortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.url.shortener.dto.AnalyticsDTO;

@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsDTO, Long> {
	
    Optional<AnalyticsDTO> findByShortCode(String shortCode);

}
