package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MediumMeterService {

    private MediumMeterRepository mediumMeterRepository;

    @Autowired
    public MediumMeterService(MediumMeterRepository mediumMeterRepository) {
        this.mediumMeterRepository = mediumMeterRepository;
    }

    public MediumMeter save(MediumMeter mediumMeter){
        return mediumMeterRepository.save(mediumMeter);
    }
}
