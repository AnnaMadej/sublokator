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

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private String number;
    private String unitName;
    private boolean active = true;


    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "meter_id")
    private Collection<Reading> readings = new ArrayList<>();

    public MediumMeter(String number, String unitName) {
        this.number = number;
        this.unitName = unitName;
    }


}
