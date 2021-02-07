package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.model.Medium;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediumRepository extends JpaRepository<Medium, Long> {
  boolean existsById(Long mediumId);
}
