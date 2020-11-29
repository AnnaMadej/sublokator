package com.aniamadej.sublokator.repository;

import com.aniamadej.sublokator.model.MediumMeter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediumMeterRepository extends CrudRepository<MediumMeter, Long> {
}
