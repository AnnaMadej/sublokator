package com.aniamadej.sublokator.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name="readings")
public class Reading {

    // == fields ==
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private LocalDate date;
    private Double reading;

    @ManyToOne
    @JoinColumn(name = "meter_id")
    private MediumMeter mediumMeter;
}
