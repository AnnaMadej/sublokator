package com.aniamadej.sublokator.dto;

import lombok.Data;

@Data
public class MediumConnectionForm {
    private String mediumName;
    private String meterNumber;
    private String meterUnit;
    private Double firstReading = .0;
}
