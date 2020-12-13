package com.aniamadej.sublokator.dto;

public interface MediumMeterBasics {
    String getMediumName();
    String getNumber();
    String getUnit();
    boolean isActive();

    void setMediumName(String mediumName);
    void setNumber(String number);
    void setUnit(String unit);
    void setActive(boolean active);
}
