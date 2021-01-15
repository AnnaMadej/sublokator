package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.ErrorMessageSource;
import com.aniamadej.sublokator.Exceptions.InputException;
import com.aniamadej.sublokator.Exceptions.MainException;
import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public
class ReadingService {

  private final ReadingRepository readingRepository;
  private final ErrorMessageSource errorMessageSource;

  @Autowired
  ReadingService(
      ReadingRepository readingRepository,
      ErrorMessageSource errorMessageSource) {
    this.readingRepository = readingRepository;
    this.errorMessageSource = errorMessageSource;
  }

  public void delete(Long readingId) {
    Reading reading = readingRepository.findById(readingId).orElseThrow(
        () -> new MainException(
            errorMessageSource.getMessage("error.noId")));
    if (readingRepository.isFirst(reading.getId())) {
      String messageCode = "error.firstDelete";
      throw new InputException(
          errorMessageSource.getMessage(messageCode));
    }
    if (reading.getReading() == 0 && !readingRepository.isLast(readingId)) {
      throw new InputException(
          errorMessageSource.getMessage("error.zeroDelete"));
    }
    readingRepository.deleteById(reading.getId());
  }

  public Long findMediumId(Long readingId) {
    return readingRepository.findMeterId(readingId).orElseThrow(
        () -> new MainException(
            errorMessageSource.getMessage("error.noId")));
  }


}
