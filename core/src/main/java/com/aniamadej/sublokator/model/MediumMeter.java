package com.aniamadej.sublokator.model;

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "medium_meters")
@NoArgsConstructor
public class MediumMeter {

  // == fields ==
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;
  private String number;
  private String unitName;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "mediumMeter")
  private Collection<Reading> readings = new ArrayList<>();
  @ManyToOne
  @JoinColumn(name = "connection_id")
  private MediumConnection mediumConnection;

  // == constructors ==
  public MediumMeter(String number, String unitName) {
    this.number = number;
    this.unitName = unitName;
  }

  // == public methods ==
  public void addReading(Reading reading) {
    reading.setMediumMeter(this);
    readings.add(reading);
  }

  public void removeReading(Reading reading) {
    readings.remove(reading);
  }

}
