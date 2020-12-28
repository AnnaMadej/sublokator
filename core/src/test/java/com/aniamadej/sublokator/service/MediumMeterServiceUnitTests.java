package com.aniamadej.sublokator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


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
import com.aniamadej.sublokator.util.ErrorCodes;
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
  private static MediumMeterRepository mockMediumMeterepository;

  @BeforeEach
  public void setUp() {
    mockReadingRepository = mock(ReadingRepository.class);
    mockMediumMeterepository = mock(MediumMeterRepository.class);
    mediumMeterService =
        new MediumMeterService(mockMediumMeterepository, mockReadingRepository);
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

    when(mockMediumMeterepository.findReadModelById(anyLong()))
        .thenReturn(Optional.of(mediumMeterBasics));
    when(mockReadingRepository.findByMediumMeterId(anyLong()))
        .thenReturn(readings);

    MediumMeterReadModel expectedMediumMeterReadModel =
        new MediumMeterReadModel(mediumMeterBasics, readings);

    MediumMeterReadModel fetchedMediumMeterReadModel =
        mediumMeterService.findById(1L);

    verify(mockMediumMeterepository, times(1))
        .findReadModelById(mediumMeterId);
    verify(mockReadingRepository, times(1))
        .findByMediumMeterId(mediumMeterId);

    assertThat(expectedMediumMeterReadModel)
        .isEqualTo(fetchedMediumMeterReadModel);
  }

  @Test
  @DisplayName("searching for medium meter by it's id should call proper method"
      + "on mediumMeterRepository and throw illegal argument exception with "
      + "appropriate message because medium meter of given id does not exist")
  public void findByIdNotExistsAndThrowsIllegalArgumentException() {
    Long mediumMeterId = 1L;

    when(mockMediumMeterepository.findReadModelById(mediumMeterId))
        .thenReturn(Optional.empty());

    Throwable exception =
        catchThrowable(() -> mediumMeterService.findById(mediumMeterId));

    verify(mockMediumMeterepository, times(1))
        .findReadModelById(mediumMeterId);
    verify(mockReadingRepository, times(0))
        .findByMediumMeterId(mediumMeterId);

    assertThat(exception).isInstanceOf(MainException.class)
        .hasMessage(
            ErrorCodes.NO_METER_ID);
  }

  @Test
  @DisplayName("adding new reading with date equal to already existing "
      + "reading of same meter throws IllegalArgumentException")
  public void addReadingDuplicateReadingDateThrowsIllegalArgumentException() {
    ReadingForm readingForm = mock(ReadingForm.class);
    when(readingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(readingForm.getReading()).thenReturn(12.);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(new MediumMeter()));

    when(mockReadingRepository
        .existsByDateAndMediumMeter(any(LocalDate.class),
            any(MediumMeter.class)))
        .thenReturn(true);

    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, readingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.DUPLICATE_READING);

    verify(mockReadingRepository, times(1))
        .existsByDateAndMediumMeter(any(LocalDate.class),
            any(MediumMeter.class));
  }

  @Test
  @DisplayName("adding new reading with date equal to date of meter reset "
      + "(another reading with 0 value) should throw IllegalArgumentException")
  public void addReadingAtMeterResetDateThrowsIllegalArgumentException() {

    ReadingForm readingForm = mock(ReadingForm.class);
    when(readingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(readingForm.getReading()).thenReturn(12.);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(true);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(new MediumMeter()));
    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, readingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.READING_AT_RESET);

    verify(mockReadingRepository, times(1))
        .isResetDate(any(LocalDate.class), anyLong());

  }

  @Test
  @DisplayName("adding new reading to medium meter of not existing id "
      + "should throw IllegalArgumentException")
  public void addReadingNotExistingMeterIdThrowsIllegalArgumentException() {

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.empty());
    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, new ReadingForm()));

    assertThat(exception).isInstanceOf(MainException.class)
        .hasMessage(ErrorCodes.NO_METER_ID);

    verify(mockMediumMeterepository, times(1))
        .findById(anyLong());

  }

  @Test
  @DisplayName("adding new reading with date before meter activation date  "
      + "should throw IllegalArgumentException")
  public void addReadingBeforeActivationDateThrowsIllegalArgumentException() {

    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn(12.);
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().plusDays(1));

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));
    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, mockReadingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.READING_BEFORE_ACTIVATION);

    verify(mockReadingRepository, times(1))
        .isResetDate(any(LocalDate.class), anyLong());
  }

  @Test
  @DisplayName("adding new reading with date after meter deactivation date  "
      + "should throw IllegalArgumentException")
  public void addReadingnAfterDeactivationDateThrowsIllegalArgumentException() {

    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn(12.);
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(LocalDate.now().minusDays(1));

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));
    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(1L, mockReadingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.READING_AFTER_DEACTIVATION);

    verify(mockReadingRepository, times(1))
        .isResetDate(any(LocalDate.class), anyLong());
  }

  @Test
  @DisplayName("adding new reading should throw IllegalArgumentException "
      + "because reading value is smaller than biggest reading "
      + "from previous date (can't insert reading smaller than preceding)")
  public void addReadingSmallerThanPrecedingThrowsIllegalArgumentException() {

    Double biggestReadingFromPreviousDate = 13.;
    ReadingForm mockReadingForm = mock(ReadingForm.class);
    Long mediumMeterId = 1L;
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading())
        .thenReturn(12.);
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(biggestReadingFromPreviousDate));


    Throwable exception = catchThrowable(
        () -> mediumMeterService.addReading(mediumMeterId, mockReadingForm));

    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.WRONG_READING_VALUE);

    verify(mockReadingRepository, times(1))
        .isResetDate(any(LocalDate.class), anyLong());
  }

  @Test
  @DisplayName("adding new reading should throw IllegalArgumentException "
      + "because reading value is bigger than previous reading "
      + "from next date (can't insert reading bigger than next "
      + "unless next is zero)")
  public void addReadingBiggerThanNextThrowsIllegalArgumentException() {
    Double previous = 12.;
    Double next = 1.;

    ReadingForm mockReadingForm = mock(ReadingForm.class);
    Long mediumMeterId = 1L;
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn(12.);
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterepository.findById(anyLong()))
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
        .hasMessage(ErrorCodes.WRONG_READING_VALUE);

    verify(mockReadingRepository, times(1))
        .isResetDate(any(LocalDate.class), anyLong());
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
    when(mockReadingForm.getReading()).thenReturn(12.);

    Long mediumMeterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);
    doNothing().when(mockMediumMeter).addReading(any(Reading.class));

    Reading reading = new Reading(LocalDate.parse(mockReadingForm.getDate()),
        mockReadingForm.getReading());
    List<Reading> readings = new ArrayList<>();
    readings.add(reading);
    when(mockMediumMeter.getReadings()).thenReturn(readings);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(previous));

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(next));

    mediumMeterService.addReading(mediumMeterId, mockReadingForm);

    verify(mockMediumMeter, times(1))
        .addReading(reading);
    verify(mockMediumMeterepository, times(1))
        .save(mockMediumMeter);
  }

  @Test
  @DisplayName(
      "Adding new reading with negative value"
          + "should throw IllegalArgumentException")
  public void addReadingNegativeValueThrowsIllegalArgumentException() {

    Double previous = -4.;
    Double next = -1.;

    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn(-2.);

    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);

    when(mockMediumMeterepository.findById(anyLong()))
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
        .hasMessage(ErrorCodes.NEGATIVE_READING);

    verify(mockMediumMeterepository, times(0))
        .save(any(MediumMeter.class));


  }

  @Test
  @DisplayName(
      "Adding new reading should be successful because readingValue is "
          + "bigger than previous and next value is zero (meter reset)"
          + "Should call save method on MediumMeterRepository")
  public void addReadingSuccessfulReadingSmallerThanNextAndNextIsZero() {

    Double previous = 12.;
    Double next = 0.;


    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn(12.);

    Long mediumMeterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);
    doNothing().when(mockMediumMeter).addReading(any(Reading.class));

    Reading reading = new Reading(LocalDate.parse(mockReadingForm.getDate()),
        mockReadingForm.getReading());
    List<Reading> readings = new ArrayList<>();
    readings.add(reading);
    when(mockMediumMeter.getReadings()).thenReturn(readings);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(previous));

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.of(next));

    mediumMeterService.addReading(mediumMeterId, mockReadingForm);

    verify(mockMediumMeter, times(1))
        .addReading(reading);
    verify(mockMediumMeterepository, times(1))
        .save(mockMediumMeter);
  }

  @Test
  @DisplayName(
      "Adding new reading should be successful because previous and "
          + "next values do not exist and active until is null")
  public void addReadingSuccessfulNextAndPreviousNotExistActUntilNull() {
    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn(12.);

    Long mediumMeterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(null);
    doNothing().when(mockMediumMeter).addReading(any(Reading.class));

    Reading reading = new Reading(LocalDate.parse(mockReadingForm.getDate()),
        mockReadingForm.getReading());
    List<Reading> readings = new ArrayList<>();
    readings.add(reading);
    when(mockMediumMeter.getReadings()).thenReturn(readings);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.empty());

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.empty());

    mediumMeterService.addReading(mediumMeterId, mockReadingForm);

    verify(mockMediumMeter, times(1))
        .addReading(reading);
    verify(mockMediumMeterepository, times(1))
        .save(mockMediumMeter);
  }

  @Test
  @DisplayName(
      "Adding new reading should be successful because previous and "
          + "next values do not exist and active until is after reading")
  public void addReadingSuccessfulNextAndPreviousNotExistActUntilAfterReading() {
    ReadingForm mockReadingForm = mock(ReadingForm.class);
    when(mockReadingForm.getDate()).thenReturn(LocalDate.now().toString());
    when(mockReadingForm.getReading()).thenReturn(12.);

    Long mediumMeterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.getActiveSince())
        .thenReturn(LocalDate.now().minusDays(5));
    when(mockMediumMeter.getActiveUntil())
        .thenReturn(LocalDate.now().plusDays(1));
    doNothing().when(mockMediumMeter).addReading(any(Reading.class));

    Reading reading = new Reading(LocalDate.parse(mockReadingForm.getDate()),
        mockReadingForm.getReading());
    List<Reading> readings = new ArrayList<>();
    readings.add(reading);
    when(mockMediumMeter.getReadings()).thenReturn(readings);

    when(mockReadingRepository
        .isResetDate(any(LocalDate.class), anyLong()))
        .thenReturn(false);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));

    when(mockReadingRepository
        .getPrevious(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.empty());

    when(mockReadingRepository
        .getNext(any(LocalDate.class), anyLong()))
        .thenReturn(Optional.empty());

    mediumMeterService.addReading(mediumMeterId, mockReadingForm);

    verify(mockMediumMeter, times(1))
        .addReading(reading);
    verify(mockMediumMeterepository, times(1))
        .save(mockMediumMeter);
  }

  @Test
  @DisplayName("deactivating mediumMeter should throw IllegalArgumentException"
      + "if date String is not parsable")
  public void deactivateNotParsableDateThrowsIllegalArgumentException() {
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
        .hasMessage(ErrorCodes.BLANK_DATE);

    assertThat(exception2).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.BLANK_DATE);

    assertThat(exception3).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.BLANK_DATE);
  }

  @Test
  @DisplayName("deactivating mediumMeter should throw IllegalArgumentException"
      + "because deactivation date is in future")
  public void deactivateFutureDateThrowsIllegalArgumentException() {
    String deactivationDate1 = LocalDate.now().plusDays(1).toString();
    Long meterId = 1L;

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate1));


    assertThat(exception1).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.FUTURE_DEACTIVATION);

  }

  @Test
  @DisplayName("deactivating mediumMeter should throw IllegalArgumentException"
      + "if medium meter not exists")
  public void deactivateNotExistingMeterThrowsIllegalArgumentException() {
    String deactivationDate1 = LocalDate.now().minusDays(1).toString();
    Long meterId = 1L;

    when(mockMediumMeterepository.existsById(meterId)).thenReturn(false);

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate1));


    assertThat(exception1).isInstanceOf(MainException.class)
        .hasMessage(ErrorCodes.NO_METER_ID);

  }

  @Test
  @DisplayName("deactivating mediumMeter should throw IllegalArgumentException"
      + "if meter deactivation date is before activation date")
  public void deactivateDeactivationBeforeActivationThrowsIllArgException() {
    String deactivationDate1 = LocalDate.now().minusDays(1).toString();
    Long meterId = 1L;

    when(mockMediumMeterepository.existsById(meterId)).thenReturn(true);
    when(mockMediumMeterepository.getActiveSince(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).plusDays(1));

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate1));


    assertThat(exception1).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.DEACTIVATION_BEFORE_ACTIVATION);

  }

  @Test
  @DisplayName("deactivating mediumMeter should throw IllegalArgumentException"
      + "if meter deactivation date is before last reading date")
  public void deactivateDeactivationBeforeLastReadingThrowsIllArgException() {
    String deactivationDate1 = LocalDate.now().minusDays(1).toString();
    Long meterId = 1L;

    when(mockMediumMeterepository.existsById(meterId)).thenReturn(true);
    when(mockMediumMeterepository.getActiveSince(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).minusDays(1));
    when(mockMediumMeterepository.getLastReadingDate(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).plusDays(1));

    Throwable exception1 = catchThrowable(
        () -> mediumMeterService.deactivate(meterId, deactivationDate1));


    assertThat(exception1).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.DEACTIVATION_BEFORE_LAST_READING);
  }

  @Test
  @DisplayName("deactivation successful should call appropriate method of"
      + "mediumMeterRepository")
  public void deactivateSuccessCallsAppropriateRepositoryMethod() {
    String deactivationDate1 = LocalDate.now().minusDays(1).toString();
    Long meterId = 1L;

    when(mockMediumMeterepository.existsById(meterId)).thenReturn(true);
    when(mockMediumMeterepository.getActiveSince(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).minusDays(1));
    when(mockMediumMeterepository.getLastReadingDate(meterId))
        .thenReturn(LocalDate.parse(deactivationDate1).minusDays(1));

    mediumMeterService.deactivate(meterId, deactivationDate1);

    ArgumentCaptor<LocalDate> dateCaptor =
        ArgumentCaptor.forClass(LocalDate.class);
    ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);

    verify(mockMediumMeterepository, times(1))
        .deactivate(longCaptor.capture(), dateCaptor.capture());

    assertThat(longCaptor.getValue())
        .isEqualTo(meterId);
    assertThat(dateCaptor.getValue())
        .isEqualTo(LocalDate.parse(deactivationDate1));
  }

  @Test
  @DisplayName("reset should throw illegalArgumentException because date is "
      + "not parsable")
  public void resetNotParsableDateThrowsIllegalArgumentException() {
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
        .hasMessage(ErrorCodes.BLANK_DATE);

    assertThat(exception2).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.BLANK_DATE);

    assertThat(exception3).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.BLANK_DATE);
  }

  @Test
  @DisplayName("reset with wrong meter id throws IllegalArgumentException"
      + "because meter nt exists")
  public void resetWrongMeterIdThrowsIllegalArgumentException() {
    String resetDate = LocalDate.now().toString();
    Long meterId = 1L;

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.empty());

    Throwable exception =
        catchThrowable(() -> mediumMeterService.reset(meterId, resetDate));
    assertThat(exception).isInstanceOf(MainException.class)
        .hasMessage(ErrorCodes.NO_METER_ID);

    ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
    verify(mockMediumMeterepository, times(1))
        .findById(longCaptor.capture());
    assertThat(longCaptor.getValue()).isEqualTo(meterId);
  }

  @Test
  @DisplayName("reset of not resettable medium meter throws IllegalArgumentException")
  public void resetNotResettableMeterIdThrowsIllegalArgumentException() {
    String resetDate = LocalDate.now().toString();
    Long meterId = 1L;
    MediumMeter mediumMeter = mock(MediumMeter.class);
    when(mediumMeter.isResettable()).thenReturn(false);
    when(mediumMeter.getId()).thenReturn(meterId);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mediumMeter));

    Throwable exception =
        catchThrowable(() -> mediumMeterService.reset(meterId, resetDate));
    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.NOT_RESETTABLE);
  }

  @Test
  @DisplayName("reset before last reading date "
      + "should throw IllegalArgumentException")
  public void resetBeforeLastReadingThrowsIllegalArgumentException() {
    String resetDate = LocalDate.now().toString();
    Long meterId = 1L;
    MediumMeter mediumMeter = mock(MediumMeter.class);
    when(mediumMeter.isResettable()).thenReturn(true);
    when(mediumMeter.getId()).thenReturn(meterId);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mediumMeter));
    when(mockMediumMeterepository.getLastReadingDate(meterId))
        .thenReturn(LocalDate.parse(resetDate).plusDays(1));

    Throwable exception =
        catchThrowable(() -> mediumMeterService.reset(meterId, resetDate));
    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.RESET_NOT_AFTER_LAST_READING);
  }

  @Test
  @DisplayName("reset at last reading date "
      + "should throw IllegalArgumentException")
  public void resetAtLastReadingThrowsIllegalArgumentException() {
    String resetDate = LocalDate.now().toString();
    Long meterId = 1L;
    MediumMeter mediumMeter = mock(MediumMeter.class);
    when(mediumMeter.isResettable()).thenReturn(true);
    when(mediumMeter.getId()).thenReturn(meterId);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mediumMeter));
    when(mockMediumMeterepository.getLastReadingDate(meterId))
        .thenReturn(LocalDate.parse(resetDate));

    Throwable exception =
        catchThrowable(() -> mediumMeterService.reset(meterId, resetDate));
    assertThat(exception).isInstanceOf(InputException.class)
        .hasMessage(ErrorCodes.RESET_NOT_AFTER_LAST_READING);
  }

  @Test
  @DisplayName("reset with success should call appropriate method"
      + " of mediumMeterRepository")
  public void resetSuccessCallsMethodOfRepository() {
    String resetDate = LocalDate.now().toString();
    Long meterId = 1L;
    MediumMeter mockMediumMeter = mock(MediumMeter.class);
    when(mockMediumMeter.isResettable()).thenReturn(true);
    when(mockMediumMeter.getId()).thenReturn(meterId);

    when(mockMediumMeterepository.findById(anyLong()))
        .thenReturn(Optional.of(mockMediumMeter));
    when(mockMediumMeterepository.getLastReadingDate(meterId))
        .thenReturn(LocalDate.parse(resetDate).minusDays(1));

    Reading reading = new Reading(LocalDate.parse(resetDate), 0D);
    doNothing().when(mockMediumMeter).addReading(any(Reading.class));

    mediumMeterService.reset(meterId, resetDate);
    verify(mockMediumMeter, times(1))
        .addReading(reading);
    verify(mockMediumMeterepository, times(1))
        .save(mockMediumMeter);
  }

  @Test
  @DisplayName("reactivation of active medium meter should not call any method"
      + "of repository")
  public void reactivationOfActiveMeterDoesNothing() {
    when(mockMediumMeterepository.isActive(anyLong())).thenReturn(true);
    mediumMeterService.reactivate(1L);
    verify(mockMediumMeterepository, times(0)).reactivate(anyLong());
  }

  @Test
  @DisplayName("reactivation of inactive medium meter should call method"
      + "of repository")
  public void reactivationOfInactiveMeterCallsRepoMethod() {
    when(mockMediumMeterepository.isActive(anyLong())).thenReturn(false);
    mediumMeterService.reactivate(1L);
    verify(mockMediumMeterepository, times(1))
        .reactivate(1L);
  }
}
