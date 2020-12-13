package com.aniamadej.sublokator.dto.input;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class ReadingForm {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull
    @Pattern(regexp = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$")
    private String date;
    @NotNull
    @Digits(integer=10, fraction=10)
    private Double reading = 0D;
}
