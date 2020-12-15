package com.aniamadej.sublokator.dto;

import java.time.LocalDate;

public interface ReadingBasics {
  LocalDate getDate();

  void setDate(LocalDate date);

  Double getReading();

  void setReading(Double reading);

  Long getId();

  void setId(Long id);

}
