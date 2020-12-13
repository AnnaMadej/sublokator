package com.aniamadej.sublokator.dto;

import java.time.LocalDate;

public interface ReadingBasics {
    LocalDate getDate();
    Double getReading();
    Long getId();

    void setDate(LocalDate date);
    void setReading(Double reading);
    void setId(Long id);}
