package com.url.shortener.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.url.shortener.dto.UrlEntity;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
	Optional<UrlEntity> findByShortCode(String shortCode);
}
											