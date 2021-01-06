package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.CustomMessageSource;
import com.aniamadej.sublokator.Exceptions.InputException;
import com.aniamadej.sublokator.Exceptions.MainException;
import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediumConnectionService {

  // == fields ==
  private final MediumConnectionRepository mediumConnectionRepository;
  private final CustomMessageSource customMessageSource;

  // == constructors ==


  @Autowired
  MediumConnectionService(
      MediumConnectionRepository mediumConnectionRepository,
      CustomMessageSource customMessageSource) {
    this.mediumConnectionRepository = mediumConnectionRepository;
    this.customMessageSource = customMessageSource;
  }

  // == public methods ==
  public List<NumberedName> getNamesList() {
    return mediumConnectionRepository.findMediaNames();
  }

  public List<NumberedName> getMeterNumbers(long mediumConnectionId,
                                            Pageable pageable,
                                            Boolean inactive) {
    if (inactive) {
      return mediumConnectionRepository
          .fetchInactiveMeterNumbers(mediumConnectionId, pageable)
          .getContent();
    }
    return mediumConnectionRepository
        .fetchActiveMeterNumbers(mediumConnectionId, pageable)
        .getContent();
  }

  public String getMediumName(long mediumConnectionId) {
    if (!existsById(mediumConnectionId)) {
      throw new MainException(customMessageSource
          .getMessage("error.connectionNotExists"));
    }
    return mediumConnectionRepository.findMediumName(mediumConnectionId)
        .orElse("");
  }

  public boolean existsById(Long mediumConnectionId) {
    return mediumConnectionRepository.existsById(mediumConnectionId);
  }

  @Transactional
  public void save(String name) {
    if (null == name || name.equals("") || name.equals(" ")) {
      throw new InputException(
          customMessageSource.getMessage("error.blankName"));
    }

    if (name.length() > 50) {
      throw new InputException(
          customMessageSource.getMessage("error.tooLongName"));
    }

    MediumConnection connection = new MediumConnection();
    connection.setMediumName(name);
    mediumConnectionRepository.save(connection);
  }


  private void addMediumMeter(Long mediumConnectionId,
                                MediumMeter mediumMeter) {
    mediumConnectionRepository.findById(mediumConnectionId)
        .ifPresentOrElse(connection -> {
          connection.addMediumMeter(mediumMeter);
        }, () -> {
          throw new MainException(
              customMessageSource
                  .getMessage("error.connectionNotExists"));
        });
  }

  @Transactional
  public void addMediumMeter(Long mediumConnectionId,
                             MediumMeterForm mediumMeterForm) {
    if (mediumMeterForm.getFirstReading() < 0) {
      throw new InputException(
          customMessageSource.getMessage("error.negativeReading"));
    }
    MediumMeter mediumMeter = mediumMeterForm.toMediumMeter();
    addMediumMeter(mediumConnectionId, mediumMeter);
  }


}
