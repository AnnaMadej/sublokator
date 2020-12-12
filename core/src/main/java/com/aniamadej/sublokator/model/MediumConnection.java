package com.aniamadej.sublokator.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="medium_connections")
@NoArgsConstructor
public class MediumConnection {

    // == fields ==
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String mediumName;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "mediumConnection")
    private List<MediumMeter> mediumMeters = new ArrayList<>();

    // == constructors ==
    public MediumConnection(String MediumName) {
        this.mediumName = MediumName;
    }


    // == public methods ==
    public void addMediumMeter(MediumMeter mediumMeter){
        mediumMeter.setMediumConnection(this);
        this.mediumMeters.add(mediumMeter);
    }

    public void removeMediumMeter(MediumMeter mediumMeter){
        this.mediumMeters.remove(mediumMeter);
    }


}
