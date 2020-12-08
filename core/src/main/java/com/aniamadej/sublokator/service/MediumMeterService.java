package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MediumMeterService {


    // == fields ==
    private final MediumMeterRepository mediumMeterRepository;

    // == constructors ==
    @Autowired
    public MediumMeterService(MediumMeterRepository mediumMeterRepository) {
        this.mediumMeterRepository = mediumMeterRepository;
    }

    // == public methods ==
    public void save(MediumMeter mediumMeter) {
        mediumMeterRepository.save(mediumMeter);
    }
}
