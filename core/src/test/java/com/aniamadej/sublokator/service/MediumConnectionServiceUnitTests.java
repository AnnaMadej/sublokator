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
import com.aniamadej.sublokator.util.ErrorMessages;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class MediumConnectionServiceUnitTests {

  private static MediumConnectionRepository mockMediumConnectionRepository;
  private static MediumConnectionService mediumConnectionService;

  @BeforeEach
  public void setUp() {
    mockMediumConnectionRepository =
        mock(MediumConnectionRepository.class);

    mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository);
  }

  @Test
  @DisplayName("adding medium meter should throw Illegal Argument "
      + "exception because initial reading is < 0")
  public void addMediumMeterNegativeReadingThrowsIllegalArgumentException() {

    MediumMeterForm mockMediumMeterForm = mock(MediumMeterForm.class);
    when(mockMediumMeterForm.getFirstReading()).thenReturn(-2.);

    Throwable exception =
        catchThrowable(() -> mediumConnectionService
            .addMediumMeter(1L, mockMediumMeterForm));

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.NEGATIVE_READING);
  }


  @Test
  @DisplayName("adding medium meter to database should throw Illegal Argument "
      + "exception because medium connection of new medium meter does not exist")
  public void addMediumMeterNotExistingMediumThrowsIllegalArgumentException() {

    when(mockMediumConnectionRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    Throwable exception =
        catchThrowable(() -> mediumConnectionService
            .addMediumMeter(1L, new MediumMeterForm()));

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.NO_MEDIUM_CONNECTION_ID);
  }


  @Test
  @DisplayName("should create and add proper medium meter to database")
  public void addMediumMeterExistingMediumAddsProperMediumToDatabase() {

    MediumMeterForm mediumMeterForm = getMediumMeterForm();
    MediumConnection mediumConnection = getMediumConnection();

    when(mockMediumConnectionRepository.findById(anyLong()))
        .thenReturn(Optional.of(mediumConnection));
    when(mockMediumConnectionRepository.save(any(MediumConnection.class)))
        .then(i -> i.getArgument(0));

    mediumConnectionService.addMediumMeter(1L, mediumMeterForm);

    ArgumentCaptor<MediumConnection> mediumConnectionCaptor =
        ArgumentCaptor.forClass(MediumConnection.class);
    verify(mockMediumConnectionRepository, times(1))
        .save(mediumConnectionCaptor.capture());
    MediumConnection savedMediumConnection = mediumConnectionCaptor.getValue();

    assertTrue(savedMediumConnection
        .getMediumMeters().stream()
        .anyMatch(mm -> mm.getNumber().equals(mediumMeterForm.getNumber())
            && mm.getUnitName().equals(mediumMeterForm.getUnitName())));

    assertTrue(savedMediumConnection.getMediumMeters()
        .stream()
        .flatMap(mm -> mm.getReadings().stream())
        .anyMatch(r ->
            r.getDate().equals(LocalDate.now())
                && r.getReading().equals(mediumMeterForm.getFirstReading())));

    assertEquals(1, savedMediumConnection.getMediumMeters().size());
    assertEquals(1,
        savedMediumConnection.getMediumMeters().get(0).getReadings().size());

  }

  @Test
  @DisplayName("should add medium connection with proper name to database")
  public void addsMediumConnectionWithGoodName() {
    String mediumName = "medium name";

    mediumConnectionService.save(mediumName);

    ArgumentCaptor<MediumConnection> argumentCaptor
        = ArgumentCaptor.forClass(MediumConnection.class);
    verify(mockMediumConnectionRepository, times(1))
        .save(argumentCaptor.capture());
    MediumConnection mediumConnection = argumentCaptor.getValue();

    assertEquals(mediumName, mediumConnection.getMediumName());
  }

  @Test
  @DisplayName("adding medium connection to database should throw "
      + "illegal argument exception because name is too long")
  public void saveTooLongMediumNameThrowsIllegalArgumentException() {

    String mediumName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    Throwable exception
        = catchThrowable(() -> mediumConnectionService.save(mediumName));

    assertThat(exception)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.TOO_LONG_NAME);
  }

  @Test
  @DisplayName("adding medium connection to database should throw "
      + "illegal argument exception because name null, empty or space")
  public void saveBlankMediumNameThrowsIllegalArgumentException() {

    String mediumName1 = " ";
    String mediumName2 = "";
    String mediumName3 = null;

    Throwable exception1
        = catchThrowable(() -> mediumConnectionService.save(mediumName1));

    Throwable exception2
        = catchThrowable(() -> mediumConnectionService.save(mediumName2));

    Throwable exception3
        = catchThrowable(() -> mediumConnectionService.save(mediumName3));

    assertThat(exception1)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.BLANK_NAME);

    assertThat(exception2)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.BLANK_NAME);

    assertThat(exception3)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(ErrorMessages.BLANK_NAME);
  }

  @Test
  @DisplayName("getting list of names should call appropriate method on "
      + "MediumConnection repository and return what it returns")
  public void getNamesListCallsAppropriateMethodAndReturnsItsReturn() {

    List<NumberedName> fetchedNames = createNamesList();

    when(mockMediumConnectionRepository.findMediaNames())
        .thenReturn(fetchedNames);

    List<NumberedName> returnedNames = mediumConnectionService.getNamesList();

    verify(mockMediumConnectionRepository, times(1))
        .findMediaNames();

    assertThat(returnedNames).isEqualTo(fetchedNames);
  }

  @Test
  @DisplayName("getting meter numbers should call appropriate method of"
      + "MediumMeterRepository - the one that fetches numbers "
      + "of inactive meters")
  public void getMeterNumbersCallsMethodToGerNumberOfInactiveMeters() {
    long connectionId = 1L;
    Pageable pageable = PageRequest.of(0, 5);

    Page<NumberedName> names = mock(Page.class);

    when(mockMediumConnectionRepository
        .fetchInactiveMeterNumbers(connectionId, pageable)).thenReturn(names);

    List<NumberedName> returnedNumbers = mediumConnectionService
        .getMeterNumbers(connectionId, pageable, true);

    verify(mockMediumConnectionRepository, times(1))
        .fetchInactiveMeterNumbers(connectionId, pageable);

    assertThat(returnedNumbers).isEqualTo(names.getContent());
  }

  @Test
  @DisplayName("getting meter numbers should call appropriate method of"
      + "MediumMeterRepository - the one that fetches numbers "
      + "of active meters")
  public void getMeterNumbersCallsMethodToGerNumberOfActiveMeters() {


    long connectionId = 1L;
    Pageable pageable = PageRequest.of(0, 5);

    Page<NumberedName> names = mock(Page.class);

    when(mockMediumConnectionRepository
        .fetchActiveMeterNumbers(connectionId, pageable)).thenReturn(names);

    List<NumberedName> returnedNumbers = mediumConnectionService
        .getMeterNumbers(connectionId, pageable, false);

    verify(mockMediumConnectionRepository, times(1))
        .fetchActiveMeterNumbers(connectionId, pageable);

    assertThat(returnedNumbers).isEqualTo(names.getContent());
  }

  @Test
  @DisplayName("Getting medium name should call appropriate method"
      + " of MediumConnectionRepository and return what it returns if exists")
  public void getMediumNameCallsAppropriateMethodAndReturnsCorrectValue() {

    String mediumName = "medium name";

    long mediumId = 1L;

    when(mockMediumConnectionRepository.findMediumName(mediumId))
        .thenReturn(Optional.of(mediumName));

    String fetchedMediumName = mediumConnectionService.getMediumName(mediumId);


    verify(mockMediumConnectionRepository, times(1))
        .findMediumName(mediumId);


    assertThat(fetchedMediumName).isEqualTo(mediumName);

  }

  @Test
  @DisplayName("Getting medium name should call appropriate method"
      + " of MediumConnectionRepository and return empty String if not exists")
  public void getMediumNameCallsAppropriateMethodAndReturnsCorrectEmptyValue() {

    long mediumId = 1L;

    when(mockMediumConnectionRepository.findMediumName(mediumId))
        .thenReturn(Optional.empty());

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

    Long mediumId1 = 1L;
    Long mediumId2 = 2L;

    when(mockMediumConnectionRepository.existsById(mediumId1)).thenReturn(true);
    when(mockMediumConnectionRepository.existsById(mediumId2))
        .thenReturn(false);

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
