package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.model.MediumConnection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MediumConnectionRepository
    extends JpaRepository<MediumConnection, Long> {
  @Query("select mc.mediumName as name, mc.id as id from MediumConnection mc")
  List<NumberedName> findMediaNames();


  @Query("select mm.number as name, mm.id as id "
      + "from MediumConnection  mc join mc.mediumMeters mm "
      + "where mc.id=:connectionId")
  Page<NumberedName> fetchMeterNumbers(long connectionId, Pageable pageable);

  @Query("select mm.number as name, mm.id as id "
      + "from MediumConnection  mc join mc.mediumMeters mm "
      + "where mc.id=:connectionId")
  List<NumberedName> fetchMeterNumbers(long connectionId);

  @Query("select mc.mediumName from MediumConnection mc "
      + "where mc.id = :connectionId")
  Optional<String> findMediumName(long connectionId);

  boolean existsById(Long mediumId);
}
