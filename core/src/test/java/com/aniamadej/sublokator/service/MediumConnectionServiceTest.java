package com.aniamadej.sublokator.service;

import com.aniamadej.sublokator.dto.MediumMeterForm;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.util.ErrorMesages;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MediumConnectionServiceTest {

    @Test
    void addMediumMeter_notExistingMedium_ThrowsIllegalArgumentException() {
        MediumConnectionRepository mockMediumConnectionRepository = mock(MediumConnectionRepository.class);
        when(mockMediumConnectionRepository.findById(anyLong())).thenReturn(Optional.empty());

        MediumConnectionService mediumConnectionService
                = new MediumConnectionService(mockMediumConnectionRepository);

        Throwable exception =
                catchThrowable(()->mediumConnectionService.addMediumMeter(1l, new MediumMeterForm()));

        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorMesages.NO_MEDIUM_CONNECTION_ID);
    }

}