package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.ErrorMessageSource;
import com.aniamadej.sublokator.Exceptions.InputException;
import com.aniamadej.sublokator.Exceptions.MainException;
import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.model.Medium;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.MediumRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MediumConnectionService {

  // == fields ==
  private final MediumConnectionRepository mediumConnectionRepository;
  private final MediumMeterRepository mediumMeterRepository;
  private final ErrorMessageSource errorMessageSource;
  private final MediumRepository mediumRepository;

  // == constructors ==
  @Autowired
  MediumConnectionService(
      MediumConnectionRepository mediumConnectionRepository,
      MediumMeterRepository mediumMeterRepository,
      ErrorMessageSource errorMessageSource,
      MediumRepository mediumRepository) {
    this.mediumConnectionRepository = mediumConnectionRepository;
    this.mediumMeterRepository = mediumMeterRepository;
    this.errorMessageSource = errorMessageSource;
    this.mediumRepository = mediumRepository;
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
    return mediumConnectionRepository.findMediumName(mediumConnectionId)
        .orElseThrow(() -> new MainException(errorMessageSource
            .getMessage("error.connectionNotExists")));
  }

  public boolean existsById(Long mediumConnectionId) {
    return mediumConnectionRepository.existsById(mediumConnectionId);
  }

  public Long save(String name, String description) {
    if (null == name || name.equals("") || name.equals(" ")) {
      throw new InputException(
          errorMessageSource.getMessage("error.blankName"));
    }

    name = name.toUpperCase().trim();

    if (name.length() > 50) {
      throw new InputException(
          errorMessageSource.getMessage("error.tooLongName"));
    }

    if (null == description || description.equals("") || description
        .equals(" ")) {
      throw new InputException(
          errorMessageSource.getMessage("error.blankDescription"));
    }

    if (description.length() > 50) {
      throw new InputException(
          errorMessageSource.getMessage("error.tooLongDescription"));
    }

    Medium medium = mediumRepository.findByName(name).orElse(new Medium(name));

    MediumConnection connection = new MediumConnection(medium, description);

    return mediumConnectionRepository.save(connection).getId();

  }


  public void addMediumMeter(Long mediumConnectionId,
                             MediumMeterForm mediumMeterForm) {

    if (Double.parseDouble(mediumMeterForm.getFirstReading()) < 0) {
      throw new InputException(
          errorMessageSource.getMessage("error.negativeReading"));
    }

    mediumConnectionRepository.findById(mediumConnectionId)
        .ifPresentOrElse(connection -> {
          MediumMeter mediumMeter = mediumMeterForm.toMediumMeter();
          mediumMeter.setMediumConnection(connection);
          mediumMeterRepository.save(mediumMeter);
        }, () -> {
          throw new MainException(
              errorMessageSource
                  .getMessage("error.connectionNotExists"));
        });
  }


}
