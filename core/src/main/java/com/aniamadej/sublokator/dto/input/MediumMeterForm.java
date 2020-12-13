package com.aniamadej.sublokator.dto.input;

import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public
class MediumMeterForm {

    // TODO: export messages to international file

    @NotBlank(message = "number cannot be null")
    private String number;

    @NotBlank(message = "unit name cannot be null")
    private String unitName;

    @Digits(integer = 10, fraction = 10, message = "wrong number format")
    private Double firstReading = 0D;

    public MediumMeter toMediumMeter() {
        MediumMeter mediumMeter = new MediumMeter();
        mediumMeter.setUnitName(this.getUnitName());
        mediumMeter.setNumber(this.getNumber());
        if(this.getFirstReading() == null){
            this.setFirstReading(0D);
        }
        Reading reading = new Reading(this.firstReading);

        mediumMeter.addReading(reading);
        return mediumMeter;
}
}
