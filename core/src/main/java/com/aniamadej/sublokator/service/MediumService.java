package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.repository.MediumRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MediumService {

  // == fields ==
  private final MediumRepository mediumRepository;

  // == constructors ==
  @Autowired
  MediumService(
      MediumRepository mediumRepository) {
    this.mediumRepository = mediumRepository;
  }


  // == public methods ==
  public List<NumberedName> getNamesList() {
    return mediumRepository.findMediaNames();
  }

}
