package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.dto.MediumMeterForm;
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
    public void save(MediumMeterForm mediumMeterForm) {
        mediumMeterRepository.save(mediumMeterForm.toMediumMeter());
    }

}
