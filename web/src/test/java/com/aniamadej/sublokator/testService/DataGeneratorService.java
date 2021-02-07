package com.aniamadej.sublokator.testService;

import com.aniamadej.sublokator.repository.MediumRepository;
import com.aniamadej.sublokator.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataGeneratorService {

  private final MediumRepository mediumRepository;
  private final ReadingRepository readingRepository;

  @Autowired
  DataGeneratorService(
      MediumRepository mediumRepository,
      ReadingRepository readingRepository) {
    this.mediumRepository = mediumRepository;
    this.readingRepository = readingRepository;
  }


  public String generateUniqueMediumName() {
    String mediumName = "medium";
    String generatedName;
    int mediumNameNumber = 0;

    do {
      mediumNameNumber++;
      generatedName = mediumName + mediumNameNumber;
    }
    while (mediumRepository.findByName(generatedName).isPresent());
    return generatedName;
  }


}
