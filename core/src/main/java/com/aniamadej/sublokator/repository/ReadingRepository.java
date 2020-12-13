package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {

    @Query("select r.date as date, r.reading as reading, r.id as id " +
            "from Reading r where r.mediumMeter.id = :id order by r.date desc, r.reading desc")
    List<ReadingBasics> findByMediumMeterId(@Param("id") Long id);

    @Query("select max(r.reading) from Reading r where r.date<:date and r.mediumMeter.id = :meterId" )
    Optional<Double> getMaxReadingBefore(@Param("date") LocalDate date, @Param("meterId") Long meterId);

    @Query("select min(r.reading) from Reading r where r.date>:date and r.mediumMeter.id = :meterId" )
    Optional<Double> getMinReadingAfter(@Param("date") LocalDate date, @Param("meterId") Long meterId);
}
