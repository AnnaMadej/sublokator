package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.dto.MediumMeterBasics;
import com.aniamadej.sublokator.model.MediumMeter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediumMeterRepository extends JpaRepository<MediumMeter, Long> {
    @Query("select " +
            "mm.mediumConnection.mediumName as mediumName, " +
            "mm.number as number," +
            "mm.unitName as unit," +
            "mm.active as active " +
            "from MediumMeter mm where mm.id = :id")
    Optional<MediumMeterBasics> findOneById(@Param("id") Long id);


}
