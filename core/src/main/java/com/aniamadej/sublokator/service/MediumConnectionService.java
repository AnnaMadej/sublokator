package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.util.ErrorMesages;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
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
    MediumConnection connection = new MediumConnection();
    connection.setMediumName(name);
    mediumConnectionRepository.save(connection);
  }

  public void addMediumMeter(Long mediumConnectionId,
                             MediumMeterForm mediumMeterForm) {
    MediumConnection mediumConnection =
        mediumConnectionRepository.findById(mediumConnectionId)
            .map(connection -> {
              MediumMeter mediumMeter = mediumMeterForm.toMediumMeter();
              connection.addMediumMeter(mediumMeter);
              return mediumConnectionRepository.save(connection);
            })
            .orElseThrow(() ->
                new IllegalArgumentException(
                    ErrorMesages.NO_MEDIUM_CONNECTION_ID));
  }

}
