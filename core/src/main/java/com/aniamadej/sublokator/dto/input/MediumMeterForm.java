package com.aniamadej.sublokator.dto.input;

import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import java.time.LocalDate;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;

@Getter
@Setter
public class MediumMeterForm {

  @NotBlank(message = "{error.empty}")
  private String meterNumber;

  @NotBlank(message = "{error.empty}")
  private String meterUnit;

  @NotNull(message = "{error.empty}")
  @DecimalMin(value = "0.0", message = "{error.onlyPositive}")
  @Digits(integer = 10, fraction = 10, message = "{error.number}")
  @NumberFormat
  private String firstReading = "0.0";

  @NotNull(message = "{error.empty}")
  @Pattern(regexp = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$",
      message = "{error.date}")
  private String activeSince = LocalDate.now().toString();

  private boolean resettable = false;

  public MediumMeter toMediumMeter() {
    MediumMeter mediumMeter = new MediumMeter();
    mediumMeter.setUnitName(this.getMeterUnit());
    mediumMeter.setNumber(this.getMeterNumber());
    LocalDate activeSinceDate = LocalDate.parse(this.getActiveSince());
    mediumMeter.setActiveSince(activeSinceDate);
    mediumMeter.setResettable(this.resettable);
    if (this.getFirstReading() == null) {
      this.setFirstReading("0.0");
    }
    Reading reading =
        new Reading(activeSinceDate, Double.parseDouble(this.firstReading));
    mediumMeter.addReading(reading);
    return mediumMeter;
  }
}
