package com.aniamadej.sublokator.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "media_types")
public class MediaType {

  private String name;
  // == fields ==
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
}
