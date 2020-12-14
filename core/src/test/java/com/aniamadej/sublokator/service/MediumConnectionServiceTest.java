package com.aniamadej.sublokator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.util.ErrorMesages;
import java.time.LocalDate;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@Slf4j
class MediumConnectionServiceTest {

  @Test
  @DisplayName("should throw Illegal Argument exception because medium "
      + "connection of new medium meter does not exist")
  void addMediumMeternotExistingMediumThrowsIllegalArgumentException() {
    MediumConnectionRepository mockMediumConnectionRepository =
        mock(MediumConnectionRepository.class);
    when(mockMediumConnectionRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    MediumConnectionService mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);

    Throwable exception =
        catchThrowable(() -> mediumConnectionService
            .addMediumMeter(1L, new MediumMeterForm()));

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMesages.NO_MEDIUM_CONNECTION_ID);
  }

  @Test
  @DisplayName("should create and add proper medium meter to database")
  void addMediumMeterExistingMediumAddsProperMediumToDatabase() {

    // prepare MediumMeterForm to be converted to new medium meter
    // and mediumConnection containing that meter
    MediumMeterForm mediumMeterForm = getMediumMeterForm();
    MediumConnection mediumConnection = getMediumConnection();

    // prepare mock of mediumConnectionRepository which returns proper
    // mediumConnection when asked
    // and returns the same mediumConnection that was passed to be saved
    MediumConnectionRepository mockMediumConnectionRepository =
        mock(MediumConnectionRepository.class);
    when(mockMediumConnectionRepository.findById(anyLong()))
        .thenReturn(Optional.of(mediumConnection));
    when(mockMediumConnectionRepository.save(any(MediumConnection.class)))
        .then(i -> i.getArgument(0));

    // prepare MediumConnectionService to be checked
    MediumConnectionService mediumConnectionService =
        new MediumConnectionService(mockMediumConnectionRepository);

    // invoke method to check it
    mediumConnectionService.addMediumMeter(1L, mediumMeterForm);

    // catch the mediumConnection that was passed
    // to be saved by mediumConnectionRepository
    // and check that save() method was invoked only once
    ArgumentCaptor<MediumConnection> mediumConnectionCaptor =
        ArgumentCaptor.forClass(MediumConnection.class);
    verify(mockMediumConnectionRepository, times(1))
        .save(mediumConnectionCaptor.capture());
    MediumConnection savedMediumConnection = mediumConnectionCaptor.getValue();

    // check if saved medium connection contains provided medium meter
    assertTrue(savedMediumConnection
        .getMediumMeters().stream()
        .anyMatch(mm -> mm.getNumber().equals(mediumMeterForm.getNumber())
            && mm.getUnitName().equals(mediumMeterForm.getUnitName())));

    // check if saved medium meter contains provided reading
    assertTrue(savedMediumConnection.getMediumMeters()
        .stream()
        .flatMap(mm -> mm.getReadings().stream())
        .anyMatch(r ->
            r.getDate().equals(LocalDate.now())
                && r.getReading().equals(mediumMeterForm.getFirstReading())));

    // check if only one medium meter and one reading were added
    assertEquals(1, savedMediumConnection.getMediumMeters().size());
    assertEquals(1,
        savedMediumConnection.getMediumMeters().get(0).getReadings().size());

  }

  private MediumConnection getMediumConnection() {
    MediumConnection mediumConnection = new MediumConnection();
    mediumConnection.setId(1L);
    mediumConnection.setMediumName("medium name");
    return mediumConnection;
  }

  private MediumMeterForm getMediumMeterForm() {
    MediumMeterForm mediumMeterForm = new MediumMeterForm();
    mediumMeterForm.setNumber("11");
    mediumMeterForm.setFirstReading(11D);
    mediumMeterForm.setUnitName("kwh");
    mediumMeterForm.setActiveSince(LocalDate.now().toString());
    return mediumMeterForm;
  }

}
