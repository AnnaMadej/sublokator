package com.aniamadej.sublokator.dto.input;

import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import java.time.LocalDate;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class MediumMeterForm {

  // TODO: export messages to international file

  @NotBlank(message = "number cannot be null")
  private String number;

  @NotBlank(message = "unit name cannot be null")
  private String unitName;

  @Digits(integer = 10, fraction = 10, message = "wrong number format")
  private Double firstReading = 0D;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @NotNull
  @Pattern(regexp = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$")
  private String activeSince;

  private boolean resettable;

  public MediumMeter toMediumMeter() {
    MediumMeter mediumMeter = new MediumMeter();
    mediumMeter.setUnitName(this.getUnitName());
    mediumMeter.setNumber(this.getNumber());
    LocalDate activeSinceDate = LocalDate.parse(this.getActiveSince());
    mediumMeter.setActiveSince(activeSinceDate);
    mediumMeter.setResettable(this.resettable);
    if (this.getFirstReading() == null) {
      this.setFirstReading(0D);
    }
    Reading reading = new Reading(activeSinceDate, this.firstReading);
    mediumMeter.addReading(reading);
    return mediumMeter;
  }
}
