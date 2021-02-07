package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.dto.MediumMeterBasics;
import com.aniamadej.sublokator.model.MediumMeter;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MediumMeterRepository
    extends JpaRepository<MediumMeter, Long> {
  @Query("select "
      + "mm.mediumConnection.description as mediumName, mm.number as number, "
      + "mm.unitName as unit, mm.resettable as resettable, "
      + "mm.activeUntil as activeUntil, mm.activeSince as activeSince "
      + "from MediumMeter mm where mm.id = :id")
  Optional<MediumMeterBasics> findReadModelById(@Param("id") Long id);

  @Modifying
  @Query("update MediumMeter m set m.activeUntil = :activeUntil "
      + "where m.id=:mediumId ")
  void deactivate(Long mediumId, LocalDate activeUntil);


  @Query("select m.activeSince from MediumMeter m where m.id = :meterId")
  LocalDate getActiveSince(@Param("meterId") Long meterId);

  @Query("select max(r.date) from Reading  r where r.mediumMeter.id =:meterId")
  Optional<LocalDate> getLastReadingDate(@Param("meterId") Long meterId);

  @Query("select m.resettable from MediumMeter m where m.id=:meterId")
  Boolean isResettable(@Param("meterId") Long meterId);

  @Query("select (m.activeUntil is null) from MediumMeter m "
      + "where m.id=:meterId")
  Boolean isActive(@Param("meterId") Long meterId);

  @Modifying
  @Query("update MediumMeter m set m.activeUntil = null "
      + "where m.id=:meterId ")
  void reactivate(Long meterId);


}
