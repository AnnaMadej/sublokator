package com.aniamadej.sublokator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.ReadingRepository;
import com.aniamadej.sublokator.util.ErrorMessages;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReadingServiceUnitTests {

  ReadingService readingService;
  ReadingRepository mockReadingRepository;

  @BeforeEach
  public void init() {
    mockReadingRepository = mock(ReadingRepository.class);
    readingService = new ReadingService(mockReadingRepository);
  }

  @Test
  @DisplayName("deletin reading that does not exist"
      + "should throw IllegalArgumentException")
  public void deleteReadingNotExistsThrowsIllegalArgumentException() {
    when(mockReadingRepository.findById(anyLong()))
        .thenReturn(Optional.empty());
    Throwable exception
        = catchThrowable(() -> readingService.delete(1L));
    assertThat(exception).isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            ErrorMessages.NO_READING_ID);
  }

  @Test
  @DisplayName("deleting first reading od meter should throw "
      + "IllealArgumentException")
  public void deleteFirstReadingThrowsIllegalArgumentException() {
    when(mockReadingRepository.findById(anyLong()))
        .thenReturn(Optional.of(new Reading()));
    when(mockReadingRepository.isFirst(anyLong()))
        .thenReturn(true);
    Throwable exception
        = catchThrowable(() -> readingService.delete(1L));
    assertThat(exception).isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            ErrorMessages.FIRST_DELETE);
  }

  @Test
  @DisplayName("deleting zero reading which is not a last reading"
      + "should throw IllegalArgumentException")
  public void deleteZeroNotLastReadingThrowsIllegalArgumentException() {
    Reading mockReading = mock(Reading.class);
    when(mockReading.getReading()).thenReturn(0.);
    when(mockReadingRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockReading));
    when(mockReadingRepository.isFirst(anyLong()))
        .thenReturn(false);
    when(mockReadingRepository.isLast(anyLong()))
        .thenReturn(false);
    Throwable exception
        = catchThrowable(() -> readingService.delete(1L));
    assertThat(exception).isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            ErrorMessages.ZERO_DELETE);
  }

  @Test
  @DisplayName("deleting reading that is not first "
      + "not last and not zero "
      + "should call delete on repository")
  public void deleteNotLastNotFirstNotZeroPerformsDeleteOnRepository() {
    Reading mockReading = mock(Reading.class);
    when(mockReading.getReading()).thenReturn(2.);
    when(mockReading.getId()).thenReturn(1L);

    when(mockReadingRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockReading));
    when(mockReadingRepository.isFirst(anyLong()))
        .thenReturn(false);
    when(mockReadingRepository.isLast(anyLong()))
        .thenReturn(false);

    readingService.delete(1L);

    verify(mockReadingRepository, times(1))
        .deleteById(mockReading.getId());
  }

  @Test
  @DisplayName("deleting reading that is not first "
      + "and is zero but last"
      + "should call delete on repository")
  public void deleteNotFirstZeroLastPerformsDeleteOnRepository() {
    Reading mockReading = mock(Reading.class);
    when(mockReading.getReading()).thenReturn(0.);
    when(mockReading.getId()).thenReturn(1L);

    when(mockReadingRepository.findById(anyLong()))
        .thenReturn(Optional.of(mockReading));
    when(mockReadingRepository.isFirst(anyLong()))
        .thenReturn(false);
    when(mockReadingRepository.isLast(anyLong()))
        .thenReturn(true);

    readingService.delete(1L);

    verify(mockReadingRepository, times(1))
        .deleteById(mockReading.getId());
  }

  @Test
  public void findMeterIdMeterNotExistsThrowsIllegalArgumentException() {
    Long readingId = 1L;
    when(mockReadingRepository.findMeterId(readingId))
        .thenReturn(Optional.empty());
    Throwable exception
        = catchThrowable(() -> readingService.findMediumId(1L));
    assertThat(exception).isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            ErrorMessages.NO_METER_ID);
    verify(mockReadingRepository, times(1))
        .findMeterId(readingId);
  }

  @Test
  public void findMeterIdMeterExistsReturnsId() {
    Long readingId = 1L;
    Long meterId = 2L;
    when(mockReadingRepository.findMeterId(readingId))
        .thenReturn(Optional.of(meterId));

    Long fetchedMeterId = readingService.findMediumId(readingId);
    verify(mockReadingRepository, times(1))
        .findMeterId(readingId);
    assertThat(meterId).isEqualTo(fetchedMeterId).isEqualTo(meterId);
  }
}
