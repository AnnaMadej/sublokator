package com.aniamadej.sublokator.model;

import javax.persistence.*;

@Entity
@Table(name = "media_types")
public class MediaType {

    // == fields ==
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    String name;
}
