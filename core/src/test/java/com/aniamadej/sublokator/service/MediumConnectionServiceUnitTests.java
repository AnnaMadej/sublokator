package com.aniamadej.sublokator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
  private static MediumMeterRepository mockMediumMeterRepository;
  private static MediumRepository mockMediumRepository;

  @BeforeEach
  public void setUp() {
    mockMediumConnectionRepository =
        mock(MediumConnectionRepository.class);
    ErrorMessageSource mockErrorMessageSource =
        mock(ErrorMessageSource.class);
    mockMediumMeterRepository = mock(MediumMeterRepository.class);

    mockMediumRepository = mock(MediumRepository.class);

    mediumConnectionService
        = new MediumConnectionService(mockMediumConnectionRepository,
        mockMediumMeterRepository, mockErrorMessageSource,
        mockMediumRepository);

    ArgumentCaptor<String> errorCodeCaptor =
        ArgumentCaptor.forClass(String.class);

    when(mockErrorMessageSource
        .getMessage(errorCodeCaptor.capture()))
        .thenAnswer(i -> errorCodeCaptor.getValue());

    when(mockMediumConnectionRepository.save(any(MediumConnection.class)))
        .then(returnsFirstArg());

  }

  @Test
  @DisplayName("adding medium meter should throw InputException "
      + "with proper message "
      + "because initial reading is < 0")
  public void addMediumMeterNegativeReadingThrowsIllegalArgumentException() {

    String errorCode = "error.negativeReading";
    MediumMeterForm mockMediumMeterForm = mock(MediumMeterForm.class);
    when(mockMediumMeterForm.getFirstReading()).thenReturn("-2.0");

    Throwable exception =
        catchThrowable(() -> mediumConnectionService
            .addMediumMeter(1L, mockMediumMeterForm));

    assertThat(exception)
        .isInstanceOf(InputException.class)
        .hasMessage(errorCode);
  }


  @Test
  @DisplayName("adding medium meter to database should throw MainException "
      + "with proper message because medium connection of new medium meter "
      + "does not exist")
  public void addMediumMeterNotExistingMediumThrowsIllegalArgumentException() {

    String errorCode = "error.connectionNotExists";
    when(mockMediumConnectionRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    Throwable exception =
        catchThrowable(() -> mediumConnectionService
            .addMediumMeter(1L, new MediumMeterForm()));

    assertThat(exception)
        .isInstanceOf(MainException.class)
        .hasMessage(errorCode);
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

    ArgumentCaptor<MediumMeter> mediumConnectionCaptor =
        ArgumentCaptor.forClass(MediumMeter.class);
    verify(mockMediumMeterRepository, times(1))
        .save(mediumConnectionCaptor.capture());
    MediumMeter savedMediumMeter = mediumConnectionCaptor.getValue();

    assertThat(savedMediumMeter.getNumber())
        .isEqualTo(mediumMeterForm.getMeterNumber());
    assertThat(savedMediumMeter.getUnitName())
        .isEqualTo(mediumMeterForm.getMeterUnit());

    assertThat(savedMediumMeter.getReadings().get(0).getReading())
        .isEqualTo(Double.parseDouble(mediumMeterForm.getFirstReading()));
    assertThat(savedMediumMeter.getReadings().get(0).getDate())
        .isEqualTo(mediumMeterForm.getActiveSince());
    assertThat(savedMediumMeter.getReadings().get(0).getMediumMeter())
        .isEqualTo(savedMediumMeter);

    assertEquals(1,
        savedMediumMeter.getReadings().size());

  }

  @Test
  @DisplayName("should add  connection with proper name to existing medium ")
  public void addsMediumConnectionWithGoodNameExistingMedium() {

    String mediumName = "medium name";
    Medium medium = new Medium(mediumName);
    when(mockMediumRepository.findByName(any(String.class)))
        .thenReturn(Optional.of(medium));

    String connectionDescription = "description";

    mediumConnectionService.save(mediumName, connectionDescription);

    ArgumentCaptor<MediumConnection> argumentCaptor
        = ArgumentCaptor.forClass(MediumConnection.class);
    verify(mockMediumConnectionRepository, times(1))
        .save(argumentCaptor.capture());
    MediumConnection mediumConnection = argumentCaptor.getValue();

    assertEquals(connectionDescription, mediumConnection.getDescription());
    assertEquals(mediumName, mediumConnection.getMedium().getName());
  }

  @Test
  @DisplayName("should add  connection with proper name to not existing medium")
  public void addsMediumConnectionWithGoodNameNewMedium() {

    String mediumName = "some medium";
    when(mockMediumRepository.findByName(any(String.class)))
        .thenReturn(Optional.empty());

    String connectionDescription = "description";

    mediumConnectionService.save(mediumName, connectionDescription);

    ArgumentCaptor<MediumConnection> argumentCaptor
        = ArgumentCaptor.forClass(MediumConnection.class);
    verify(mockMediumConnectionRepository, times(1))
        .save(argumentCaptor.capture());
    MediumConnection mediumConnection = argumentCaptor.getValue();

    assertEquals(connectionDescription, mediumConnection.getDescription());
    assertEquals(mediumName.toUpperCase().trim(),
        mediumConnection.getMedium().getName());
  }

  @Test
  @DisplayName("adding medium connection to database should throw "
      + "illegal argument exception because name is too long")
  public void saveTooLongMediumNameThrowsIllegalArgumentException() {

    String errorCode = "error.tooLongName";

    String mediumName = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    String mediumDescription = "aa ";

    Throwable exception
        = catchThrowable(
        () -> mediumConnectionService.save(mediumName, mediumDescription));

    assertThat(exception)
        .isInstanceOf(InputException.class)
        .hasMessage(errorCode);
  }

  @Test
  @DisplayName("adding medium connection to database should throw "
      + "InputException with proper message  "
      + "because name is null, empty or space")
  public void saveBlankMediumNameThrowsIllegalArgumentException() {
    String errorCode = "error.blankName";

    String mediumDescription = "aa ";

    String mediumName1 = " ";
    String mediumName2 = "";
    String mediumName3 = null;

    Throwable exception1
        = catchThrowable(
        () -> mediumConnectionService.save(mediumName1, mediumDescription));

    Throwable exception2
        = catchThrowable(
        () -> mediumConnectionService.save(mediumName2, mediumDescription));

    Throwable exception3
        = catchThrowable(
        () -> mediumConnectionService.save(mediumName3, mediumDescription));

    assertThat(exception1)
        .isInstanceOf(InputException.class)
        .hasMessage(errorCode);

    assertThat(exception2)
        .isInstanceOf(InputException.class)
        .hasMessage(errorCode);

    assertThat(exception3)
        .isInstanceOf(InputException.class)
        .hasMessage(errorCode);
  }

  @Test
  @DisplayName("adding medium connection to database should throw "
      + "illegal argument exception because description is too long")
  public void saveTooLongDescriptionThrowsIllegalArgumentException() {

    String errorCode = "error.tooLongDescription";

    String mediumName = "aa";
    String mediumDescription =
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa ";

    Throwable exception
        = catchThrowable(
        () -> mediumConnectionService.save(mediumName, mediumDescription));

    assertThat(exception)
        .isInstanceOf(InputException.class)
        .hasMessage(errorCode);
  }

  @Test
  @DisplayName("adding medium connection to database should throw "
      + "InputException with proper message  "
      + "because description is null, empty or space")
  public void saveBlankDescriptionThrowsIllegalArgumentException() {
    String errorCode = "error.blankDescription";

    String mediumName = "aa ";

    String mediumDescription1 = " ";
    String mediumDescription2 = "";
    String mediumDescription3 = null;

    Throwable exception1
        = catchThrowable(
        () -> mediumConnectionService.save(mediumName, mediumDescription1));

    Throwable exception2
        = catchThrowable(
        () -> mediumConnectionService.save(mediumName, mediumDescription2));

    Throwable exception3
        = catchThrowable(
        () -> mediumConnectionService.save(mediumName, mediumDescription3));

    assertThat(exception1)
        .isInstanceOf(InputException.class)
        .hasMessage(errorCode);

    assertThat(exception2)
        .isInstanceOf(InputException.class)
        .hasMessage(errorCode);

    assertThat(exception3)
        .isInstanceOf(InputException.class)
        .hasMessage(errorCode);
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
  @DisplayName("Getting medium name should throw Main exception "
      + " as medium not exists")
  public void getMediumNameThrowsMainExceptionMediumNotExists() {
    long mediumId = 1L;

    when(mockMediumConnectionRepository.findMediumName(mediumId))
        .thenReturn(Optional.empty());

    Throwable exception =
        catchThrowable(() -> mediumConnectionService.getMediumName(mediumId));

    assertThat(exception).isInstanceOf(MainException.class)
        .hasMessage("error.connectionNotExists");

  }

  @Test
  @DisplayName("Getting medium name should call appropriate method"
      + " of MediumConnectionRepository and return what it returns")
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
    mediumMeterForm.setMeterNumber("11");
    mediumMeterForm.setFirstReading("11");
    mediumMeterForm.setMeterUnit("kwh");
    mediumMeterForm.setActiveSince(LocalDate.now().toString());
    return mediumMeterForm;
  }

  private MediumConnection getMediumConnection() {
    MediumConnection mediumConnection = new MediumConnection();
    mediumConnection.setId(1L);
    mediumConnection.setDescription("medium name");
    return mediumConnection;
  }

}
