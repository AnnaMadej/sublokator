package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.dto.input.ReadingForm;
import com.aniamadej.sublokator.dto.output.MediumMeterReadModel;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MediumMeterService {


    // == fields ==
    private final MediumMeterRepository mediumMeterRepository;
    private final ReadingRepository readingRepository;

    // == constructors ==
    @Autowired
    MediumMeterService(MediumMeterRepository mediumMeterRepository, ReadingRepository readingRepository) {
        this.mediumMeterRepository = mediumMeterRepository;
        this.readingRepository = readingRepository;
    }

    // == public methods ==
    public void save(MediumMeterForm mediumMeterForm) {
        mediumMeterRepository.save(mediumMeterForm.toMediumMeter());
    }

    public Optional<MediumMeterReadModel> findById(long meterId) {
        return mediumMeterRepository.findOneById(meterId).map(meter -> {
            List<ReadingBasics> readings = readingRepository.findByMediumMeterId(meterId);
            MediumMeterReadModel mediumMeterReadModel = new MediumMeterReadModel(meter, readings);
            return mediumMeterReadModel;
        });
    }


    public void addReading(Long meterId, ReadingForm readingForm) {
        MediumMeter mediumMeter = mediumMeterRepository.findById(meterId)
                .orElseThrow(()->new IllegalArgumentException("error.meterNotExists"));
        Reading reading = new Reading(readingForm.getDate(), readingForm.getReading());
        mediumMeter.addReading(reading);
        mediumMeterRepository.save(mediumMeter);
    }
}
