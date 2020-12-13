package com.aniamadej.sublokator.dto.input;

import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Getter
@Setter
public
class MediumMeterForm {

    // TODO: export messages to international file

    @NotBlank(message = "number cannot be null")
    private String number;

    @NotBlank(message = "unit name cannot be null")
    private String unitName;

    @Digits(integer=10, fraction=10, message = "wrong number format")
    private Double firstReading = 0.;

    public MediumMeter toMediumMeter() {
        MediumMeter mediumMeter = new MediumMeter();
        mediumMeter.setUnitName(this.getUnitName());
        mediumMeter.setNumber(this.getNumber());

        if (null != this.getFirstReading() && this.getFirstReading() != 0) {
            Reading reading = new Reading();
            reading.setReading(this.firstReading);
            reading.setDate(LocalDate.now());
            mediumMeter.addReading(reading);
        }
        return mediumMeter;
    }
}
