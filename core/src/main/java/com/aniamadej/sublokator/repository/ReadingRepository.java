package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {

  @Query("select r.date as date, r.reading as reading, r.id as id "
      + "from Reading r where r.mediumMeter.id = :meterId "
      + "order by r.date desc, r.reading desc")
  List<ReadingBasics> findByMediumMeterId(@Param("meterId") Long meterId);

  @Query("select r.reading from Reading r "
      + "where r.mediumMeter.id = :meterId "
      + "and r.date = (select max(r1.date) from Reading r1 "
      + "where  r.mediumMeter.id = :meterId and r1.date < :date) "
      + "order by r.date desc")
  Optional<Double> getMaxReadingBefore(@Param("date") LocalDate date,
                                       @Param("meterId") Long meterId);

  @Query("select r.reading from Reading r "
      + "where r.mediumMeter.id = :meterId "
      + "and r.date = (select min(r1.date) from Reading r1 "
      + "where  r.mediumMeter.id = :meterId and r1.date > :date) "
      + "order by r.date desc")
  Optional<Double> getMinReadingAfter(@Param("date") LocalDate date,
                                      @Param("meterId") Long meterId);


  @Query("select case when (count(r) > 0) then true else false end "
      + "from Reading r where r.mediumMeter.id=:meterId "
      + "and r.reading = 0 and r.date=:readingDate")
  Boolean isResetDate(@Param("readingDate") LocalDate readingDate,
                      @Param("meterId") Long meterId);

  Boolean existsByDateAndMediumMeter(LocalDate date, MediumMeter mediumMeter);

  @Query("select case when count(r) = 0 then true else false end "
      + "from Reading r "
      + "where r.date<(select r1.date from Reading r1 where r1.id=:readingId) "
      + "and r.mediumMeter.id=(select r1.mediumMeter.id "
      + "from Reading r1 where r1.id=:readingId)")
  Boolean isFirst(@Param("readingId") Long readingId);

  @Query("select case when count(r) = 0 then true else false end "
      + "from Reading r "
      + "where r.date>(select r1.date from Reading r1 where r1.id=:readingId) "
      + "and r.mediumMeter.id=(select r1.mediumMeter.id "
      + "from Reading r1 where r1.id=:readingId)")
  Boolean isLast(@Param("readingId") Long readingId);
}
