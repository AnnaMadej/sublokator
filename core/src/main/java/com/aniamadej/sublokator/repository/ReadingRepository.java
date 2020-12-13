package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {

    @Query("select r.date as date, r.reading as reading, r.id as id from Reading r where r.mediumMeter.id = :id")
    List<ReadingBasics> findByMediumMeterId(@Param("id") Long id);
}
