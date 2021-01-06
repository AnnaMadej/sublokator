package com.aniamadej.sublokator.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE,
      generator = "media_type_sequence")
  @SequenceGenerator(name = "media_type_sequence",
      sequenceName = "media_type_sequence", allocationSize = 1)
  private long id;
}
