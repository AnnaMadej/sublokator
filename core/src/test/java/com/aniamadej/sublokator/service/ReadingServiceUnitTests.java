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
import com.aniamadej.sublokator.util.ErrorMesages;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
  public void deleteReadingNotExistsThrowsIllegalArgumentException() {
    when(mockReadingRepository.findById(anyLong()))
        .thenReturn(Optional.empty());
    Throwable exception
        = catchThrowable(() -> readingService.delete(1L));
    assertThat(exception).isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            ErrorMesages.NO_READING_ID);
  }

  @Test
  public void deleteFirstReadingThrowsIllegalArgumentException() {
    when(mockReadingRepository.findById(anyLong()))
        .thenReturn(Optional.of(new Reading()));
    when(mockReadingRepository.isFirst(anyLong()))
        .thenReturn(true);
    Throwable exception
        = catchThrowable(() -> readingService.delete(1L));
    assertThat(exception).isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            ErrorMesages.FIRST_DELETE);
  }

  @Test
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
            ErrorMesages.ZERO_DELETE);
  }

  @Test
  public void deletePerformsDeleteOnRepository() {
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
}
