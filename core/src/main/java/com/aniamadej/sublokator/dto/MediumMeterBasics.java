package com.aniamadej.sublokator.dto;

import java.time.LocalDate;

public interface MediumMeterBasics {
  String getMediumName();

  void setMediumName(String mediumName);

  String getNumber();

  void setNumber(String number);

  String getUnit();

  void setUnit(String unit);

  LocalDate getActiveSince();

  void setActiveSince(LocalDate localDate);

  LocalDate getActiveUntil();

  void setActiveUntil(LocalDate localDate);

}
