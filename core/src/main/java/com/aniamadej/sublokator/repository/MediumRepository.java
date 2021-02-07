package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.model.Medium;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MediumRepository extends JpaRepository<Medium, Long> {
  @Query("select m.name as name, m.id as id from Medium m")
  List<NumberedName> findMediaNames();

  boolean existsById(Long mediumId);
}
