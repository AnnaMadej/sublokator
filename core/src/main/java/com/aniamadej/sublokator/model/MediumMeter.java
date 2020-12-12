package com.aniamadej.sublokator.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;

@Data
@Entity
@Table(name="medium_meters")
@NoArgsConstructor
public class MediumMeter {

    // == fields ==
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String number;
    private String unitName;
    private boolean active = true;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "mediumMeter")
    private Collection<Reading> readings = new ArrayList<>();

    // == constructors ==
    public MediumMeter(String number, String unitName) {
        this.number = number;
        this.unitName = unitName;
    }

    // == public methods ==
    public void addReading(Reading reading){
        reading.setMediumMeter(this);
        readings.add(reading);
    }

    public void removeReading(Reading reading){
        readings.remove(reading);
    }

    @ManyToOne
    @JoinColumn(name = "connection_id")
    private MediumConnection mediumConnection;

}
