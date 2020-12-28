package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.util.ErrorMessages;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MediumConnectionService {

  // == fields ==
  private final MediumConnectionRepository mediumConnectionRepository;

  // == constructors ==
  @Autowired
  MediumConnectionService(
      MediumConnectionRepository mediumConnectionRepository) {
    this.mediumConnectionRepository = mediumConnectionRepository;
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
        .orElse("");
  }

  public boolean existsById(Long mediumConnectionId) {
    return mediumConnectionRepository.existsById(mediumConnectionId);
  }

  public void save(String name) {
    if (null == name || name.equals("") || name.equals(" ")) {
      throw new IllegalArgumentException(ErrorMessages.BLANK_NAME);
    }

    if (name.length() > 50) {
      throw new IllegalArgumentException(ErrorMessages.TOO_LONG_NAME);
    }

    MediumConnection connection = new MediumConnection();
    connection.setMediumName(name);
    mediumConnectionRepository.save(connection);
  }

  public void addMediumMeter(Long mediumConnectionId,
                             MediumMeterForm mediumMeterForm) {

    if (mediumMeterForm.getFirstReading() < 0) {
      throw new IllegalArgumentException(ErrorMessages.NEGATIVE_READING);
    }

    mediumConnectionRepository.findById(mediumConnectionId)
        .map(connection -> {
          MediumMeter mediumMeter = mediumMeterForm.toMediumMeter();
          connection.addMediumMeter(mediumMeter);
          return mediumConnectionRepository.save(connection);
        })
        .orElseThrow(() ->
            new IllegalArgumentException(
                ErrorMessages.NO_MEDIUM_CONNECTION_ID));
  }

}
