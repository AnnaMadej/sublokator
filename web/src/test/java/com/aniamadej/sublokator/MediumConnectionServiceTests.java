package com.aniamadej.sublokator;

import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.ReadingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;

@SpringBootTest
class MediumConnectionServiceTests {

    @Autowired
    private ReadingRepository readingRpository;

    @Autowired
    private MediumConnectionRepository mediumConnectionRepository;

    @Autowired
    private MediumMeterRepository mediumMeterRepository;

    @Test
    void contextLoads() {
    }

    @Rollback(false)
    @Test
    void addsReading(){
        Reading reading = new Reading();
        reading.setDate(LocalDate.now());
        reading.setReading(71830.2);
        MediumMeter mediumMeter = new MediumMeter();
        mediumMeter.addReading(reading);
        MediumConnection mediumConnection = new MediumConnection();
        mediumConnection.addMediumMeter(mediumMeter);
        mediumConnectionRepository.save(mediumConnection);
    }

}
