package com.aniamadej.sublokator.model;

import java.time.LocalDate;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "readings")
public class Reading {

  // == fields ==
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private LocalDate date;
  private Double reading;

  @ManyToOne(cascade = {CascadeType.PERSIST})
  @JoinColumn(name = "meter_id")
  private MediumMeter mediumMeter;

  public Reading(LocalDate date, Double reading) {
    this.reading = reading;
    this.date = date;
  }

  public void setMediumMeter(MediumMeter mediumMeter) {
    if (this.mediumMeter != mediumMeter) {
      this.mediumMeter = mediumMeter;
      mediumMeter.addReading(this);
    }
  }

}
