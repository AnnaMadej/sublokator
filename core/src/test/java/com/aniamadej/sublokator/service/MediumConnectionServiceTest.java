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


import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.dto.input.MediumMeterForm;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.util.ErrorMesages;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class MediumConnectionServiceTest {

  @Test
  @DisplayName("adding connection to database should throw Illegal Argument "
      + "exception because medium connection of new medium meter does not exist")
  public void addMediumMeterNotExistingMediumThrowsIllegalArgumentException() {
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
  public void addMediumMeterExistingMediumAddsProperMediumToDatabase() {

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

  @Test
  @DisplayName("should add medium connection with proper name to database")
  public void addsMediumConnectionWithGoodName() {
    // Prepare fake instance of MediumConnectionRepository
    MediumConnectionRepository mockMediumConnectionRepository
        = mock(MediumConnectionRepository.class);

    // Create medium name to be passed as argument of creating
    // new MediumConnection
    String mediumName = "medium name";

    // prepare MediumConnectionService to be checked
    MediumConnectionService mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);

    // Invoke method to check it
    mediumConnectionService.save(mediumName);

    // catch argument passed to MediumConnectionRepository method
    // and check if it was invoked only once
    ArgumentCaptor<MediumConnection> argumentCaptor
        = ArgumentCaptor.forClass(MediumConnection.class);
    verify(mockMediumConnectionRepository, times(1))
        .save(argumentCaptor.capture());
    MediumConnection mediumConnection = argumentCaptor.getValue();

    // check if argument passed to MediumConnectionRepository has name
    // provided at the beginning
    assertEquals(mediumName, mediumConnection.getMediumName());
  }

  @Test
  @DisplayName("adding medium connection to database should throw "
      + "illegal argument exception because name is too long")
  public void saveTooLongMediumNameThrowsIllegalArgumentException() {

    // Prepare fake instance of MediumConnectionRepository
    MediumConnectionRepository mockMediumConnectionRepository
        = mock(MediumConnectionRepository.class);

    // Create medium name to be passed as argument of creating
    // new MediumConnection
    String mediumName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    MediumConnectionService mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);

    // Catch exception MediumConnectionService throws while saving
    Throwable exception
        = catchThrowable(() -> mediumConnectionService.save(mediumName));

    // Check if exception is the instance of good class and has proper message
    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMesages.TOO_LONG_NAME);
  }

  @Test
  @DisplayName("adding medium connection to database should throw "
      + "illegal argument exception because name null, empty or space")
  public void saveBlankMediumNameThrowsIllegalArgumentException() {

    // Prepare fake instance of MediumConnectionRepository
    MediumConnectionRepository mockMediumConnectionRepository
        = mock(MediumConnectionRepository.class);

    // Create medium names to be passed as argument of creating
    // new MediumConnections
    String mediumName1 = " ";
    String mediumName2 = "";
    String mediumName3 = null;
    MediumConnectionService mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);

    // Catch exceptions MediumConnectionService throws while saving
    Throwable exception1
        = catchThrowable(() -> mediumConnectionService.save(mediumName1));

    Throwable exception2
        = catchThrowable(() -> mediumConnectionService.save(mediumName2));

    Throwable exception3
        = catchThrowable(() -> mediumConnectionService.save(mediumName3));

    // Check if exceptions are the instances of good class and have
    // proper message
    assertThat(exception1)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMesages.BLANK_NAME);

    assertThat(exception2)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMesages.BLANK_NAME);

    assertThat(exception3)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMesages.BLANK_NAME);
  }

  @Test
  @DisplayName("getting list of names should call appropriate method on "
      + "MediumConnection repository and return what it returns")
  public void getNamesListCallsAppropriateMethodAndReturnsItsReturn() {
    // Prepare fake instance of MediumConnectionRepository
    MediumConnectionRepository mockMediumConnectionRepository
        = mock(MediumConnectionRepository.class);

    // Prepare list of names to be received from mockConnectionRepository
    List<NumberedName> fetchedNames = createNamesList();

    when(mockMediumConnectionRepository.findMediaNames())
        .thenReturn(fetchedNames);

    // Prepare real MediumConnectionService to check it
    MediumConnectionService mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);

    // Call method to check it
    List<NumberedName> returnedNames = mediumConnectionService.getNamesList();

    // verify if aproppriate method of MediumConnectionRepository is called
    // and if it is called only once
    verify(mockMediumConnectionRepository, times(1))
        .findMediaNames();

    // check if values received from repository are equal to the ones returned
    assertThat(returnedNames).isEqualTo(fetchedNames);
  }

  @Test
  @DisplayName("getting meter numbers should call appropriate method of"
      + "MediumMeterRepository - the one that fetches numbers "
      + "of inactive meters")
  public void getMeterNumbersCallsMethodToGerNumberOfInactiveMeters() {
    // Prepare fake instance of MediumConnectionRepository
    MediumConnectionRepository mockMediumConnectionRepository
        = mock(MediumConnectionRepository.class);

    // Prepare variables to be passed to method
    long connectionId = 1L;
    Pageable pageable = PageRequest.of(0, 5);

    //  Prepare mock variable to be returned by mock repository
    Page<NumberedName> names = mock(Page.class);

    when(mockMediumConnectionRepository
        .fetchInactiveMeterNumbers(connectionId, pageable)).thenReturn(names);

    // Prepare real MediumConnectionService
    MediumConnectionService mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);

    // Call method to check it
    List<NumberedName> returnedNumbers = mediumConnectionService
        .getMeterNumbers(connectionId, pageable, true);

    // verify if appropriate method of MediumConnectionRepository is called
    // and if it is called only once
    verify(mockMediumConnectionRepository, times(1))
        .fetchInactiveMeterNumbers(connectionId, pageable);

    // check if values received from repository are equal to the ones returned
    assertThat(returnedNumbers).isEqualTo(names.getContent());
  }

  @Test
  @DisplayName("getting meter numbers should call appropriate method of"
      + "MediumMeterRepository - the one that fetches numbers "
      + "of active meters")
  public void getMeterNumbersCallsMethodToGerNumberOfActiveMeters() {
    // Prepare fake instance of MediumConnectionRepository
    MediumConnectionRepository mockMediumConnectionRepository
        = mock(MediumConnectionRepository.class);

    // Prepare variables to be passed to method
    long connectionId = 1L;
    Pageable pageable = PageRequest.of(0, 5);

    //  Prepare mock variable to be returned by mock repository
    Page<NumberedName> names = mock(Page.class);

    when(mockMediumConnectionRepository
        .fetchActiveMeterNumbers(connectionId, pageable)).thenReturn(names);

    // Prepare real MediumConnectionService
    MediumConnectionService mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);

    // Call method to check it
    List<NumberedName> returnedNumbers = mediumConnectionService
        .getMeterNumbers(connectionId, pageable, false);

    // verify if appropriate method of MediumConnectionRepository is called
    // and if it is called only once
    verify(mockMediumConnectionRepository, times(1))
        .fetchActiveMeterNumbers(connectionId, pageable);

    // check if values received from repository are equal to the ones returned
    assertThat(returnedNumbers).isEqualTo(names.getContent());
  }

  @Test
  @DisplayName("Getting medium name should call appropriate method"
      + " of MediumConnectionRepository and return what it returns if exists")
  public void getMediumNameCallsAppropriateMethodAndReturnsCorrectValue() {
    // Prepare fake instance of MediumConnectionRepository
    MediumConnectionRepository mockMediumConnectionRepository
        = mock(MediumConnectionRepository.class);

    // Prepare name to be returned by mockMediumConnectionRepository
    String mediumName = "medium name";

    // Create medium id to be passed as argument for fetching medium name
    long mediumId = 1L;

    when(mockMediumConnectionRepository.findMediumName(mediumId))
        .thenReturn(Optional.of(mediumName));

    // Prepare real MediumConnectionService
    MediumConnectionService mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);

    String fetchedMediumName = mediumConnectionService.getMediumName(mediumId);


    verify(mockMediumConnectionRepository, times(1))
        .findMediumName(mediumId);


    assertThat(fetchedMediumName).isEqualTo(mediumName);

  }

  @Test
  @DisplayName("Getting medium name should call appropriate method"
      + " of MediumConnectionRepository and return empty String if not exists")
  public void getMediumNameCallsAppropriateMethodAndReturnsCorrectEmptyValue() {
    // Prepare fake instance of MediumConnectionRepository
    MediumConnectionRepository mockMediumConnectionRepository
        = mock(MediumConnectionRepository.class);

    // Create medium id to be passed as argument for fetching medium name
    long mediumId = 1L;

    when(mockMediumConnectionRepository.findMediumName(mediumId))
        .thenReturn(Optional.empty());


    // Prepare real MediumConnectionService
    MediumConnectionService mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);

    String fetchedMediumName = mediumConnectionService.getMediumName(mediumId);


    verify(mockMediumConnectionRepository, times(1))
        .findMediumName(mediumId);


    assertThat(fetchedMediumName).isEqualTo("");

  }

  @Test
  @DisplayName("checking if mediumConnection exists by id should call "
      + "appropriate method of mediumConnectionRepository and return what it "
      + "returns ")
  public void existByIdCallsMethodOnRepositoryAndReturnsItsResult() {
    // Prepare fake instance of MediumConnectionRepository
    MediumConnectionRepository mockMediumConnectionRepository
        = mock(MediumConnectionRepository.class);

    // Create medium id to be passed as argument for repository
    Long mediumId1 = 1L;
    Long mediumId2 = 2L;

    when(mockMediumConnectionRepository.existsById(mediumId1)).thenReturn(true);
    when(mockMediumConnectionRepository.existsById(mediumId2))
        .thenReturn(false);

    MediumConnectionService mediumConnectionService =
        new MediumConnectionService(mockMediumConnectionRepository);

    Boolean result1 = mediumConnectionService.existsById(mediumId1);
    Boolean result2 = mediumConnectionService.existsById(mediumId2);

    verify(mockMediumConnectionRepository, times(1))
        .existsById(mediumId1);

    verify(mockMediumConnectionRepository, times(1))
        .existsById(mediumId2);

    assertThat(result1).isEqualTo(true);
    assertThat(result2).isEqualTo(false);

  }


  private List<NumberedName> createNamesList() {
    List<NumberedName> listOfNames = new ArrayList<>();
    listOfNames.add(new NumberedName() {
      @Override
      public long getId() {
        return 1;
      }

      @Override
      public String getName() {
        return "name1";
      }

      @Override
      public String setId(Long id) {
        return null;
      }

      @Override
      public String setName(String name) {
        return null;
      }
    });

    listOfNames.add(new NumberedName() {
      @Override
      public long getId() {
        return 2;
      }

      @Override
      public String getName() {
        return "name2";
      }

      @Override
      public String setId(Long id) {
        return null;
      }

      @Override
      public String setName(String name) {
        return null;
      }
    });

    listOfNames.add(new NumberedName() {
      @Override
      public long getId() {
        return 3;
      }

      @Override
      public String getName() {
        return "name3";
      }

      @Override
      public String setId(Long id) {
        return null;
      }

      @Override
      public String setName(String name) {
        return null;
      }
    });

    return listOfNames;
  }


  private MediumMeterForm getMediumMeterForm() {
    MediumMeterForm mediumMeterForm = new MediumMeterForm();
    mediumMeterForm.setNumber("11");
    mediumMeterForm.setFirstReading(11D);
    mediumMeterForm.setUnitName("kwh");
    mediumMeterForm.setActiveSince(LocalDate.now().toString());
    return mediumMeterForm;
  }

  private MediumConnection getMediumConnection() {
    MediumConnection mediumConnection = new MediumConnection();
    mediumConnection.setId(1L);
    mediumConnection.setMediumName("medium name");
    return mediumConnection;
  }

}
