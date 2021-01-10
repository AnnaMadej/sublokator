package com.aniamadej.sublokator.dto.input;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.NumberFormat;

@Getter
@Setter
public class ReadingForm {
  @NotNull(message = "{error.empty}")
  @Pattern(regexp = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$",
  message = "{error.date}")
  private String date;
  @NotNull(message = "{error.empty}")
  @Digits(integer = 10, fraction = 10, message = "{error.number}")
  @NumberFormat
  private String reading = "0.0";
}
