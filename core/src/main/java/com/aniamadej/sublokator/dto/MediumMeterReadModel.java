package com.aniamadej.sublokator.dto;

import com.aniamadej.sublokator.model.MediumMeter;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class MediumMeterReadModel {
    private String number;
    private String unit;
    private List<ReadingReadModel> readings;
    private boolean active;

    public MediumMeterReadModel(MediumMeter mediumMeter) {
        this.number = mediumMeter.getNumber();
        this.unit = mediumMeter.getUnitName();
        this.active = mediumMeter.isActive();

        final Double[] previousReadingValue = {0D};
        this.readings =
                mediumMeter.getReadings()
                        .stream()
                        .map(reading -> new ReadingReadModel(reading, previousReadingValue[0]))
                        .peek(reading -> {
                            previousReadingValue[0] = reading.getReading();
                        })
                        .collect(Collectors.toList());
    }
}
