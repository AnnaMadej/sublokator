package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.Exceptions.InputException;
import com.aniamadej.sublokator.Exceptions.MainException;
import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.ReadingRepository;
import com.aniamadej.sublokator.util.ErrorCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public
class ReadingService {

  private final ReadingRepository readingRepository;

  @Autowired
  ReadingService(
      ReadingRepository readingRepository) {
    this.readingRepository = readingRepository;
  }

  public void delete(Long readingId) {

    Reading reading = readingRepository.findById(readingId).orElseThrow(
        () -> new MainException(ErrorCodes.NO_READING_ID));
    if (readingRepository.isFirst(reading.getId())) {
      throw new InputException(ErrorCodes.FIRST_DELETE);
    }
    if (reading.getReading() == 0 && !readingRepository.isLast(readingId)) {
      throw new InputException(ErrorCodes.ZERO_DELETE);
    }
    readingRepository.deleteById(reading.getId());
  }

  public Long findMediumId(Long readingId) {
    return readingRepository.findMeterId(readingId).orElseThrow(
        () -> new MainException(ErrorCodes.NO_METER_ID));
  }
}
