package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.dto.NameDto;
import com.aniamadej.sublokator.model.MediumConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MediumConnectionRepository extends JpaRepository<MediumConnection, Long> {
    @Query("select mc.mediumName as name, mc.id as id from MediumConnection mc")
    public List<NameDto> fetchMediaNames();


    @Query("select mm.number as name, mm.id as id from MediumConnection  mc join mc.mediumMeters mm where mc.id=:connectionId and mm.active=:active")
    public Page<NameDto> fetchMeterNumbers(long connectionId, boolean active, Pageable pageable);

    @Query("select mm.number as name, mm.id as id from MediumConnection  mc join mc.mediumMeters mm where mc.id=:connectionId")
    public List<NameDto> fetchMeterNumbers(long connectionId);

    @Query("select mc.mediumName from MediumConnection mc where mc.id = :connectionId")
    public String fetchMediumName(long connectionId);
}
