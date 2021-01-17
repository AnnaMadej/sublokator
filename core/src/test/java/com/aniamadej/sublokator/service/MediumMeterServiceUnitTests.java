package com.aniamadej.sublokator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.aniamadej.sublokator.ErrorMessageSource;
import com.aniamadej.sublokator.Exceptions.InputException;
import com.aniamadej.sublokator.Exceptions.MainException;
import com.aniamadej.sublokator.dto.MediumMeterBasics;
import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.dto.input.ReadingForm;
import com.aniamadej.sublokator.dto.output.MediumMeterReadModel;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.ReadingRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MediumMeterServiceUnitTests {

  private static MediumMeterService mediumMeterService;
  private static ReadingRepository mockReadingRepository;
  private static MediumMeterRepository mockMediumMeterRepository;

  @BeforeEach
  public void setUp() {
    mockReadingRepository = mock(ReadingRepository.class);
    mockMediumMeterRepository = mock(MediumMeterRepository.class);
    ErrorMessageSource mockErrorMessageSource =
        mock(ErrorMessageSource.class);

    mediumMeterService =
        new MediumMeterService(mockMediumMeterRepository, mockReadingRepository,
            mockErrorMessageSource);


    ArgumentCaptor<String> errorCodeCaptor =
        ArgumentCaptor.forClass(String.class);

    when(mockErrorMessageSource
        .getMessage(errorCodeCaptor.capture()))
        .thenAnswer(i -> errorCodeCaptor.getValue());
  }

  @Test
  @DisplayName("searching for medium meter by it's id should call proper method"
      + "on mediumMeterRepository and return appropriate MediumMeterReadModel"
      + "because it exists ")
  public void findByIdExistsAndReturnsGoodValue() {
    Long mediumMeterId = 1L;
    MediumMeterBasics mediumMeterBasics = mock(MediumMeterBasics.class);
    when(mediumMeterBasics.getActiveSince()).thenReturn(LocalDate.now());
    when(mediumMeterBasics.getMediumName()).thenReturn("Medium Name");
    when(mediumMeterBasics.getActiveUntil())
        .thenReturn(LocalDate.now().plusYears(1));
    when(mediumMeterBasics.getNumber()).thenReturn("12A");
    when(mediumMeterBasics.getUnit()).thenReturn("unit");

    ReadingBasics readingBasics1 = mock(ReadingBasics.class);
    when(readingBasics1.getDate()).thenReturn(LocalDate.now().plusDays(2));
    when(readingBasics1.getId()).thenReturn(2L);
    when(readingBasics1.getReading()).thenReturn(12.3);

    ReadingBasics readingBasics2 = mock(ReadingBasics.class);
    when(readingBasics2.getDate()).thenReturn(LocalDate.now().plusDays(3));
    when(readingBasics2.getId()).thenReturn(3L);
    when(readingBasics2.getReading()).thenReturn(14.);

    List<ReadingBasics> readings = new ArrayList<>();
    readings.add(readingBasics1);
    readings.add(readingBasics2);

    when(mockMediumMeterRepository.findReadModelById(anyLong()))
        .thenReturn(Optional.of(mediumMeterBasics));
    when(mockReadingRepository.findByMediumMeterId(anyLong()))
        .thenReturn(readings);

    MediumMeterReadModel expectedMediumMeterReadModel =
        new MediumMeterReadModel(mediumMeterBasics, readings);

    MediumMeterReadModel fetchedMediumMeterReadModel =
        mediumMeterService.findById(1L);

    verify(mockMediumMeterRepository, times(1))
        .findReadModelById(mediumMeterId);
    verify(mockReadingRepository, times(1))
        .findByMediumMeterId(mediumMeterId);

    assertThat(expectedMediumMeterReadModel)
        .isEqualTo(fetchedMediumMeterReadModel);
  }

  @Test
  @DisplayName("searching for medium meter by it's id should call proper method"
      + "on mediumMeterRepository and throw MainException with "
      + "appropriate message because medium meter of given id does not exist")
  public void findByIdNotExistsAndThrowsMainException() {
    String errorMessage = "error.meterNotExists";
    Long mediumMeterId = 1L;

    when(mockMediumMeterRepository.findReadModelById(mediumMeterId))
        .thenReturn(Optional.empty());

    Throwable exception =
        catchThrowable(() -> mediumMeterService.findById(mediumMeterId));

    verify(mockMediumMeterRepository, times(1))
        .findReadModelById(mediumMeterId);
    verify(mockReadingRepository, times(0))
        .findByMediumMeterId(mediumMeterId);

    assertThat(exception).isInstanceOf(MainException.class)
        .hasMessage(
            errorMessage);
  }

  @Test
  @DisplayName("adding new reading with date equal to already existing "
      + "reading of same meter throws InputException with proper message")
  public void addReadingDuplicateReadingDateThrowsInputException() {
    String errorMessage = "error.duplicateReading";

    ReadingForm readingForm = mock(ReadingForm.class);
    when(readingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(readingForm.getReading()).thenReturn("12.0");

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(new MediumMeter()));

    when(mockReadingRepository
        .existsByDateAndMediumMeter(any(LocalDate.class),
            any(MediumMeter.class)))
        .thenReturn(true);

    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, readingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

    verify(mockReadingRepository, times(1))
        .existsByDateAndMediumMeter(any(LocalDate.class),
            any(MediumMeter.class));
  }


  @Test
  @DisplayName("adding new reading to medium meter of not existing id "
      + "should throw MainException with proper message")
  public void addReadingNotExistingMeterIdThrowsMainException() {
    String errorMessage = "error.meterNotExists";
    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.empty());
    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, new ReadingForm()));

    assertThat(exception).isInstanceOf(MainException.class)
        .hasMessage(errorMessage);

    verify(mockMediumMeterRepository, times(1))
        .findById(anyLong());

  }

  @Test
  @DisplayName("adding new reading with date before meter activation date  "
      + "should throw InputException with proper message")
  public void addReadingBeforeActivationDateThrowsInputException() {
    String errorMessage = "error.readingBeforeActivation";

    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn("12.0");
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().plusDays(1));

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));
    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, mockReadingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);
  }

  @Test
  @DisplayName("adding new reading with date after meter deactivation date  "
      + "should throw InputException with proper message")
  public void addReadingnAfterDeactivationDateThrowsInputException() {
    String errorMessage = "error.readingAfterDeactivation";
    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn("12.0");
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(LocalDate.now().minusDays(1));

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));
    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, mockReadingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

  }

  @Test
  @DisplayName("adding new reading should throw InputException with proper "
      + "message because reading value is smaller than biggest reading "
      + "from previous date (can't insert reading smaller than preceding)")
  public void addReadingSmallerThanPrecedingThrowsInputException() {
    String errorMessage = "error.wrongReadingValue";
    Double biggestReadingFromPreviousDate = 13.0;
    ReadingForm mockReadingForm = mock(ReadingForm.class);
    Long mediumMeterId = 1L;
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading())
        .thenReturn("12.0");
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(biggestReadingFromPreviousDate));


    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(mediumMeterId, mockReadingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

  }

  @Test
  @DisplayName("adding new reading should throw InputException "
      + "with proper message because reading value is bigger than "
      + "previous reading from next date "
      + "(can't insert reading bigger than next unless next is zero)")
  public void addReadingBiggerThanNextThrowsInputException() {
    String errorMessage = "error.wrongReadingValue";
    Double previous = 12.;
    Double next = 1.;

    ReadingForm mockReadingForm = mock(ReadingForm.class);
    Long mediumMeterId = 1L;
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn("12.0");
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(previous));

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(next));


    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(mediumMeterId, mockReadingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

  }

  @Test
  @DisplayName(
      "Adding new reading should be successful because readingValue is "
          + "bigger than previous and smaller than non zero next. "
          + "Should call save method on MediumMeterRepository")
  public void addReadingSuccessfulReadingSmallerThanNextBiggerThanPrevious() {
    Double previous = 12.;
    Double next = 13.;

    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn("12.0");

    Long mediumMeterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);
    doNothing().when(mockMediumMeter).addReading(any(Reading.class));

    Reading reading = new Reading(LocalDate.parse(mockReadingForm.getDate()),
        Double.parseDouble(mockReadingForm.getReading()));
    reading.setMediumMeter(mockMediumMeter);
    List<Reading> readings = new ArrayList<>();
    readings.add(reading);
    when(mockMediumMeter.getReadings()).thenReturn(readings);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(previous));

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(next));

    mediumMeterService.addReading(mediumMeterId, mockReadingForm);

    verify(mockMediumMeter, times(2))
        .addReading(reading);
    verify(mockReadingRepository, times(1))
        .save(reading);
  }

  @Test
  @DisplayName(
      "Adding new reading with negative value"
          + "should throw InputException with proper message")
  public void addReadingNegativeValueThrowsInputException() {
    String errorMessage = "error.negativeReading";
    Double previous = -4.;
    Double next = -1.;

    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn("-2.0");

    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));
    when(mockReadingRepository.existsByDateAndMediumMeter(any(LocalDate.class),
        any(MediumMeter.class))).thenReturn(false);

    when(mockReadingRepository.isResetDate(any(LocalDate.class),
        anyLong())).thenReturn(false);

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(previous));

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(next));

    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, mockReadingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

    verify(mockMediumMeterRepository, times(0))
        .save(any(MediumMeter.class));


  }

  @Test
  @DisplayName(
      "Adding new reading should be successful because readingValue is "
          + "bigger than previous and next value is zero (meter reset)"
          + "Should call save method on readingRepository")
  public void addReadingSuccessfulReadingSmallerThanNextAndNextIsZero() {

    Double previous = 12.;
    Double next = 0.;
    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn("12.0");

    Long mediumMeterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);
    doNothing().when(mockMediumMeter).addReading(any(Reading.class));

    Reading reading = new Reading(LocalDate.parse(mockReadingForm.getDate()),
        Double.parseDouble(mockReadingForm.getReading()));
    reading.setMediumMeter(mockMediumMeter);

    List<Reading> readings = new ArrayList<>();
    readings.add(reading);
    when(mockMediumMeter.getReadings()).thenReturn(readings);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(previous));

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(next));

    mediumMeterService.addReading(mediumMeterId, mockReadingForm);

    verify(mockMediumMeter, times(2))
        .addReading(reading);

    verify(mockReadingRepository, times(1)).save(reading);


  }

  @Test
  @DisplayName(
      "Adding new reading should be successful because previous and "
          + "next values do not exist and active until is null")
  public void addReadingSuccessfulNextAndPreviousNotExistActUntilNull() {
    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn("12.0");

    Long mediumMeterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);
    doNothing().when(mockMediumMeter).addReading(any(Reading.class));

    Reading reading = new Reading(LocalDate.parse(mockReadingForm.getDate()),
        Double.parseDouble(mockReadingForm.getReading()));
    reading.setMediumMeter(mockMediumMeter);
    List<Reading> readings = new ArrayList<>();
    readings.add(reading);
    when(mockMediumMeter.getReadings()).thenReturn(readings);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.empty());

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.empty());

    mediumMeterService.addReading(mediumMeterId, mockReadingForm);

    verify(mockMediumMeter, times(2))
        .addReading(reading);
    verify(mockReadingRepository, times(1))
        .save(reading);
  }

  @Test
  @DisplayName(
      "Adding new reading should be successful because previous and "
          + "next values do not exist and active until is after reading")
  public void addReadingSuccessfulNextAndPreviousNotExistActUntilAfterReading() {
    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn("12.0");

    Long mediumMeterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(LocalDate.now().plusDays(1));
    doNothing().when(mockMediumMeter).addReading(any(Reading.class));

    Reading reading = new Reading(LocalDate.parse(mockReadingForm.getDate()),
        Double.parseDouble(mockReadingForm.getReading()));
    reading.setMediumMeter(mockMediumMeter);
    List<Reading> readings = new ArrayList<>();
    readings.add(reading);
    when(mockMediumMeter.getReadings()).thenReturn(readings);

    when(mockReadingRepository
        .existsByDateAndMediumMeter(any(LocalDate.class),
            any(MediumMeter.class)))
        .thenReturn(false);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.empty());

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.empty());

    mediumMeterService.addReading(mediumMeterId, mockReadingForm);

    verify(mockReadingRepository, times(1)).save(reading);

    verify(mockMediumMeter, times(2))
        .addReading(reading);

  }

  @Test
  @DisplayName("deactivating mediumMeter should throw InputException"
      + "with proper message if date String is not parsable")
  public void deactivateNotParsableDateThrowsInputException() {
    String errorMessage = "error.blankDate";
    String deactivationDate1 = "aaaaa";
    String deactivationDate2 = "";
    String deactivationDate3 = " ";
    Long meterId = 1L;

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate1));

    Throwable exception2 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate2));

    Throwable exception3 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate3));

    assertThat(exception1).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

    assertThat(exception2).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

    assertThat(exception3).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);
  }

  @Test
  @DisplayName("deactivating mediumMeter should throw InputException"
      + "with proper message because deactivation date is in future")
  public void deactivateFutureDateThrowsInputException() {
    String errorMessage = "error.futureDeactivation";
    String deactivationDate1 = LocalDate.now().plusDays(1).toString();
    Long meterId = 1L;

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate1));


    assertThat(exception1).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

  }

  @Test
  @DisplayName("deactivating mediumMeter should throw MainException with "
      + "proper message if medium meter not exists")

  public void deactivateNotExistingMeterThrowsMainException() {
    String errorMessage = "error.meterNotExists";
    String deactivationDate1 = LocalDate.now().minusDays(1).toString();
    Long meterId = 1L;

    when(mockMediumMeterRepository.existsById(meterId)).thenReturn(false);

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate1));


    assertThat(exception1).isInstanceOf(MainException.class)
        .hasMessage(errorMessage);

  }

  @Test
  @DisplayName("deactivating mediumMeter should throw InputException with "
      + "proper message if meter deactivation date is before activation date")
  public void deactivateDeactivationBeforeActivationThrowsInputException() {
    String errorMessage = "error.deactivationBeforeActivation";
    String deactivationDate1 = LocalDate.now().minusDays(1).toString();
    Long meterId = 1L;

    when(mockMediumMeterRepository.existsById(meterId)).thenReturn(true);
    when(mockMediumMeterRepository.getActiveSince(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).plusDays(1));

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate1));


    assertThat(exception1).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

  }

  @Test
  @DisplayName("deactivating mediumMeter should throw InputException with "
      + "proper message if meter deactivation date is before last reading date")
  public void deactivateDeactivationBeforeLastReadingThrowsInputException() {
    String errorMessage = "error.deactivationBeforeLastReading";
    String deactivationDate1 = LocalDate.now().minusDays(1).toString();
    Long meterId = 1L;

    when(mockMediumMeterRepository.existsById(meterId)).thenReturn(true);
    when(mockMediumMeterRepository.getActiveSince(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).minusDays(1));
    when(mockMediumMeterRepository.getLastReadingDate(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).plusDays(1));

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate1));


    assertThat(exception1).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);
  }

  @Test
  @DisplayName("deactivation successful should call appropriate method of"
      + "mediumMeterRepository")
  public void deactivateSuccessCallsAppropriateRepositoryMethod() {
    String deactivationDate1 = LocalDate.now().minusDays(1).toString();
    Long meterId = 1L;

    when(mockMediumMeterRepository.existsById(meterId)).thenReturn(true);
    when(mockMediumMeterRepository.getActiveSince(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).minusDays(1));
    when(mockMediumMeterRepository.getLastReadingDate(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).minusDays(1));

    mediumMeterService.deactivate(meterId, deactivationDate1);

    ArgumentCaptor<LocalDate> dateCaptor =
        ArgumentCaptor.forClass(LocalDate.class);
    ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);

    verify(mockMediumMeterRepository, times(1))
        .deactivate(longCaptor.capture(), dateCaptor.capture());

    assertThat(longCaptor.getValue())
        .isEqualTo(meterId);
    assertThat(dateCaptor.getValue())
        .isEqualTo(LocalDate.parse(deactivationDate1));
  }

  @Test
  @DisplayName("reset should throw InputException with proper message  "
      + "because date is not parsable")
  public void resetNotParsableDateThrowsInputException() {
    String errorMessage = "error.blankDate";
    String resetDate1 = "aaaaa";
    String resetDate2 = "";
    String resetDate3 = " ";
    Long meterId = 1L;

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.reset(meterId, resetDate1));

    Throwable exception2 = catchThrowable(
        () -> mediumMeterService.reset(meterId, resetDate2));

    Throwable exception3 = catchThrowable(
        () -> mediumMeterService.reset(meterId, resetDate3));


    assertThat(exception1).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

    assertThat(exception2).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);

    assertThat(exception3).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);
  }

  @Test
  @DisplayName("reset with wrong meter id throws MainException with proper "
      + "message because meter id doesnt exists")
  public void resetWrongMeterIdThrowsMainException() {
    String errorMessage = "error.meterNotExists";
    String resetDate = LocalDate.now().toString();
    Long meterId = 1L;

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    Throwable exception =
        catchThrowable(() -> mediumMeterService.reset(meterId, resetDate));
    assertThat(exception).isInstanceOf(MainException.class)
        .hasMessage(errorMessage);

    ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockMediumMeterRepository, times(1))
        .findById(longCaptor.capture());
    assertThat(longCaptor.getValue()).isEqualTo(meterId);
  }

  @Test
  @DisplayName("reset of not resettable medium meter throws InputException "
      + "with proper message")
  public void resetNotResettableMeterIdThrowsInputException() {
    String errorMessage = "error.notResettable";
    String resetDate = LocalDate.now().toString();
    Long meterId = 1L;
    MediumMeter mediumMeter = mock(MediumMeter.class);
    when(mediumMeter.isResettable()).thenReturn(false);
    when(mediumMeter.getId()).thenReturn(meterId);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mediumMeter));

    Throwable exception =
        catchThrowable(() -> mediumMeterService.reset(meterId, resetDate));
    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);
  }

  @Test
  @DisplayName("reset before last reading date "
      + "should throw InputException with proper message")
  public void resetBeforeLastReadingThrowsInputException() {
    String errorMessage = "error.resetNotAfterLastReading";
    String resetDate = LocalDate.now().toString();
    Long meterId = 1L;
    MediumMeter mediumMeter = mock(MediumMeter.class);
    when(mediumMeter.isResettable()).thenReturn(true);
    when(mediumMeter.getId()).thenReturn(meterId);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mediumMeter));
    when(mockMediumMeterRepository.getLastReadingDate(meterId))
        .thenReturn(LocalDate.parse(resetDate).plusDays(1));

    Throwable exception =
        catchThrowable(() -> mediumMeterService.reset(meterId, resetDate));
    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);
  }

  @Test
  @DisplayName("reset at last reading date "
      + "should throw InputException with proper message")
  public void resetAtLastReadingThrowsInputException() {
    String errorMessage = "error.resetNotAfterLastReading";
    String resetDate = LocalDate.now().toString();
    Long meterId = 1L;
    MediumMeter mediumMeter = mock(MediumMeter.class);
    when(mediumMeter.isResettable()).thenReturn(true);
    when(mediumMeter.getId()).thenReturn(meterId);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mediumMeter));
    when(mockMediumMeterRepository.getLastReadingDate(meterId))
        .thenReturn(LocalDate.parse(resetDate));

    Throwable exception =
        catchThrowable(() -> mediumMeterService.reset(meterId, resetDate));
    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(errorMessage);
  }

  @Test
  @DisplayName("reset with success should call appropriate method"
      + " of readingRepository")
  public void resetSuccessCallsMethodOfRepository() {
    LocalDate resetDate = LocalDate.now();
    Long meterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.isResettable()).thenReturn(true);
    when(mockMediumMeter.getId()).thenReturn(meterId);

    when(mockMediumMeterRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));
    when(mockMediumMeterRepository.getLastReadingDate(meterId))
        .thenReturn(resetDate.minusDays(1));

    ArgumentCaptor<Reading> readingCaptor =
        ArgumentCaptor.forClass(Reading.class);

    when(mockReadingRepository.save(readingCaptor.capture()))
        .thenReturn(readingCaptor.capture());

    mediumMeterService.reset(meterId, resetDate.toString());

    Reading reading = readingCaptor.getValue();
    assertEquals(mockMediumMeter, reading.getMediumMeter());
    assertEquals(resetDate, reading.getDate());
    assertEquals(0D, reading.getReading());
  }

  @Test
  @DisplayName("reactivation of active medium meter should not call any method"
      + "of repository")
  public void reactivationOfActiveMeterDoesNothing() {
    when(mockMediumMeterRepository.isActive(anyLong())).thenReturn(true);
    mediumMeterService.reactivate(1L);
    verify(mockMediumMeterRepository, times(0)).reactivate(anyLong());
  }

  @Test
  @DisplayName("reactivation of inactive medium meter should call method"
      + "of repository")
  public void reactivationOfInactiveMeterCallsRepoMethod() {
    when(mockMediumMeterRepository.isActive(anyLong())).thenReturn(false);
    mediumMeterService.reactivate(1L);
    verify(mockMediumMeterRepository, times(1))
        .reactivate(1L);
  }
}
