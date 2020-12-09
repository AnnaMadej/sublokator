package com.aniamadej.sublokator.dto;

import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public
class MediumMeterForm {
    private String number;
    private String unitName;
    private Double firstReading;

    public MediumMeter toMediumMeter(){
        MediumMeter mediumMeter = new MediumMeter();
        mediumMeter.setUnitName(this.getUnitName());
        mediumMeter.setNumber(this.getNumber());
        Reading reading = new Reading();
        reading.setReading(this.firstReading);
        reading.setDate(LocalDate.now());
        mediumMeter.addReading(reading);
        return mediumMeter;
    }
}
