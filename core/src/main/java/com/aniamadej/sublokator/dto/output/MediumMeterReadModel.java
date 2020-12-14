package com.aniamadej.sublokator.dto.output;

import com.aniamadej.sublokator.dto.MediumMeterBasics;
import com.aniamadej.sublokator.dto.ReadingBasics;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MediumMeterReadModel implements MediumMeterBasics {
  private String mediumName;
  private String number;
  private String unit;
  private List<ReadingBasics> readings;


  public MediumMeterReadModel(MediumMeterBasics mediumMeterBasics,
                              List<ReadingBasics> readings) {
    this.number = mediumMeterBasics.getNumber();
    this.mediumName = mediumMeterBasics.getMediumName();
    this.unit = mediumMeterBasics.getUnit();
    this.readings = readings;
  }
}
