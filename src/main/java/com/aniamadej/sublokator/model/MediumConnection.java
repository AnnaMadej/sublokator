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

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String mediumName;

    public MediumConnection(String MediumName) {
        this.mediumName = MediumName;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "connection_id")
    private List<MediumMeter> mediumMeters = new ArrayList<>();

    public void addMediumMeter(MediumMeter mediumMeter){
        this.mediumMeters.add(mediumMeter);
    }

    public void removeMediumMeter(MediumMeter mediumMeter){
        this.mediumMeters.remove(mediumMeter);
    }


}
