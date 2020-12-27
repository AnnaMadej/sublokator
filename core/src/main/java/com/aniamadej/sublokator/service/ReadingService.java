package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.ReadingRepository;
import com.aniamadej.sublokator.util.ErrorMesages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class ReadingService {

  private final ReadingRepository readingRepository;

  @Autowired
  ReadingService(
      ReadingRepository readingRepository) {
    this.readingRepository = readingRepository;
  }

  public void delete(Long readingId) {

    Reading reading = readingRepository.findById(readingId).orElseThrow(
        () -> new IllegalArgumentException(ErrorMesages.NO_READING_ID));
    if (readingRepository.isFirst(reading.getId())) {
      throw new IllegalArgumentException(ErrorMesages.FIRST_DELETE);
    }
    if (reading.getReading() == 0 && !readingRepository.isFirst(readingId)) {
      throw new IllegalArgumentException(ErrorMesages.ZERO_DELETE);
    }
    readingRepository.deleteById(reading.getId());
  }
}
