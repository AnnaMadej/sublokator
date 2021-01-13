package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.CustomMessageSource;
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
  private final CustomMessageSource customMessageSource;

  @Autowired
  ReadingService(
      ReadingRepository readingRepository,
      CustomMessageSource customMessageSource) {
    this.readingRepository = readingRepository;
    this.customMessageSource = customMessageSource;
  }

  public void delete(Long readingId) {
    Reading reading = readingRepository.findById(readingId).orElseThrow(
        () -> new MainException(
            customMessageSource.getMessage("error.noId")));
    if (readingRepository.isFirst(reading.getId())) {
      String messageCode = "error.firstDelete";
      throw new InputException(
          customMessageSource.getMessage(messageCode));
    }
    if (reading.getReading() == 0 && !readingRepository.isLast(readingId)) {
      throw new InputException(
          customMessageSource.getMessage("error.zeroDelete"));
    }
    readingRepository.deleteById(reading.getId());
  }

  public Long findMediumId(Long readingId) {
    return readingRepository.findMeterId(readingId).orElseThrow(
        () -> new MainException(
            customMessageSource.getMessage("error.noId")));
  }


}
