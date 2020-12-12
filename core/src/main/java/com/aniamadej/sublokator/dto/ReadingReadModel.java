package com.aniamadej.sublokator.dto;

import com.aniamadej.sublokator.model.Reading;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
class ReadingReadModel {
    private Long id;
    private LocalDate date;
    private Double reading;
    private Double usage = 0D;

    public ReadingReadModel(Reading reading, Double previousReadingValue){
        this.date = reading.getDate();
        this.reading = reading.getReading();
        this.id = reading.getId();
        this.usage = reading.getReading() - previousReadingValue;
    }
}
