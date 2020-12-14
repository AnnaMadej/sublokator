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
  LocalDate getActiveUntil();

  void setActiveSince(LocalDate localDate);
  void setActiveUntil(LocalDate localDate);

}
