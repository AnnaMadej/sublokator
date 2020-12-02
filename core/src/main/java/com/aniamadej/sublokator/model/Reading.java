package com.aniamadej.sublokator.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
@Table(name="readings")
public class Reading {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;
    private LocalDate date;
    private Double reading;
}
