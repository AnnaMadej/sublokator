package com.aniamadej.sublokator.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "medium_connections")
@NoArgsConstructor
public class MediumConnection {

  // == fields ==
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  private String mediumName;

  @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "mediumConnection")
  private List<MediumMeter> mediumMeters = new ArrayList<>();

  // == constructors ==
  public MediumConnection(String mediumName) {
    this.mediumName = mediumName;
  }

  // == public methods ==
  public void addMediumMeter(MediumMeter mediumMeter) {
    if (!mediumMeters.contains(mediumMeter)) {
      this.mediumMeters.add(mediumMeter);
      mediumMeter.setMediumConnection(this);
    }
  }

  public void removeMediumMeter(MediumMeter mediumMeter) {
    this.mediumMeters.remove(mediumMeter);
  }


}
