package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.Exceptions.InputException;
import com.aniamadej.sublokator.Exceptions.MainException;
import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.dto.input.ReadingForm;
import com.aniamadej.sublokator.dto.output.MediumMeterReadModel;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.ReadingRepository;
import com.aniamadej.sublokator.util.ErrorCodes;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediumMeterService {


  // == fields ==
  private final MediumMeterRepository mediumMeterRepository;
  private final ReadingRepository readingRepository;

  // == constructors ==
  @Autowired
  MediumMeterService(MediumMeterRepository mediumMeterRepository,
                     ReadingRepository readingRepository) {
    this.mediumMeterRepository = mediumMeterRepository;
    this.readingRepository = readingRepository;
  }

  // == public methods ==
  public MediumMeterReadModel findById(long meterId) {
    return mediumMeterRepository.findReadModelById(meterId).map(meter -> {
      List<ReadingBasics> readings =
          readingRepository.findByMediumMeterId(meterId);
      return new MediumMeterReadModel(meter, readings);
    }).orElseThrow(
        () -> new MainException(ErrorCodes.NO_METER_ID));
  }


  public void addReading(Long meterId, ReadingForm readingForm) {
    MediumMeter mediumMeter = getMediumMeter(meterId);
    LocalDate readingDate = parseDate(readingForm.getDate());

    if (readingRepository
        .existsByDateAndMediumMeter(readingDate, mediumMeter)) {
      throw new InputException(ErrorCodes.DUPLICATE_READING);
    }

    if (readingRepository.isResetDate(readingDate, meterId)) {
      throw new InputException(
          ErrorCodes.READING_AT_RESET);
    }
    if (mediumMeter.getActiveSince().isAfter(readingDate)) {
      throw new InputException(
          ErrorCodes.READING_BEFORE_ACTIVATION);
    }

    if (null != mediumMeter.getActiveUntil()
        && mediumMeter.getActiveUntil().isBefore(readingDate)) {
      throw new InputException(
          ErrorCodes.READING_AFTER_DEACTIVATION);
    }

    Double readingValue = readingForm.getReading();
    checkIfInTheMiddleOfPreviousAndNext(meterId, readingDate,
        readingValue);
    addReading(mediumMeter, readingDate, readingValue);
  }

  @Transactional
  public void deactivate(Long meterId, String deactivationDate) {
    LocalDate activeUntil = parseDate(deactivationDate);

    if (activeUntil.isAfter(LocalDate.now())) {
      throw new InputException(ErrorCodes.FUTURE_DEACTIVATION);
    }

    if (!mediumMeterRepository.existsById(meterId)) {
      throw new MainException(ErrorCodes.NO_METER_ID);
    }

    if (activeUntil.isBefore(findActiveSinceDate(meterId))) {
      throw new InputException(
          ErrorCodes.DEACTIVATION_BEFORE_ACTIVATION);
    }

    if (activeUntil
        .isBefore(mediumMeterRepository.getLastReadingDate(meterId))) {
      throw new InputException(
          ErrorCodes.DEACTIVATION_BEFORE_LAST_READING);
    }

    mediumMeterRepository
        .deactivate(meterId, activeUntil);
  }

  @Transactional
  public void reactivate(Long meterId) {
    if (!mediumMeterRepository.isActive(meterId)) {
      mediumMeterRepository.reactivate(meterId);
    }
  }


  public void reset(Long meterId, String resetDate) {
    LocalDate dateOfReset = parseDate(resetDate);
    MediumMeter mediumMeter = getMediumMeter(meterId);

    if (!mediumMeter.isResettable()) {
      throw new InputException(ErrorCodes.NOT_RESETTABLE);
    }
    if (
        mediumMeterRepository.getLastReadingDate(meterId)
            .isAfter(dateOfReset)
            || mediumMeterRepository.getLastReadingDate(meterId)
            .isEqual(dateOfReset)) {
      throw new InputException(
          ErrorCodes.RESET_NOT_AFTER_LAST_READING);
    }

    addReading(mediumMeter, dateOfReset, 0D);

  }


  // == private methods ==
  private LocalDate parseDate(String deactivationDate) {
    LocalDate activeUntil;
    try {
      activeUntil = LocalDate.parse(deactivationDate);
    } catch (Exception e) {
      throw new InputException(ErrorCodes.BLANK_DATE);
    }
    return activeUntil;
  }


  private MediumMeter getMediumMeter(Long meterId) {
    return mediumMeterRepository.findById(meterId)
        .orElseThrow(
            () -> new MainException(ErrorCodes.NO_METER_ID));
  }

  private void addReading(MediumMeter mediumMeter, LocalDate readingDate,
                          Double readingValue) {

    if (readingValue < 0) {
      throw new InputException(ErrorCodes.NEGATIVE_READING);
    }

    Reading reading = new Reading(readingDate, readingValue);
    mediumMeter.addReading(reading);
    mediumMeterRepository.save(mediumMeter);
  }

  private LocalDate findActiveSinceDate(Long mediumMeterId) {
    return mediumMeterRepository.getActiveSince(mediumMeterId);
  }

  private void checkIfInTheMiddleOfPreviousAndNext(Long meterId,
                                                   LocalDate readingDate,
                                                   Double readingValue) {
    Double previous =
        readingRepository.getPrevious(readingDate, meterId).orElse(0D);
    Double next =
        readingRepository.getNext(readingDate, meterId)
            .orElse(readingValue + 1);

    if (readingValue < previous
        || (readingValue > next && next != 0)) {
      throw new InputException(ErrorCodes.WRONG_READING_VALUE);
    }
  }

}
