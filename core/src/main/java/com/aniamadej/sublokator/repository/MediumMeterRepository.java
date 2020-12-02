package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.model.MediumMeter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediumMeterRepository extends JpaRepository<MediumMeter, Long> {
}
