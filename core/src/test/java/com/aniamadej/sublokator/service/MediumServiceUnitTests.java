package com.aniamadej.sublokator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.repository.MediumRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MediumServiceUnitTests {

  private static MediumRepository mockMediumRepository;
  private MediumService mediumService;
  List<NumberedName> names = createNamesList();

  @BeforeEach
  public void setUp() {
    mockMediumRepository = mock(MediumRepository.class);
    when(mockMediumRepository.findMediaNames())
        .thenReturn(names);

    mediumService = new MediumService(mockMediumRepository);
  }


  @Test
  @DisplayName("getting list of names should call appropriate method on "
      + "Medium repository and return what it returns")
  public void getNamesListCallsAppropriateMethodAndReturnsItsReturn() {
    List<NumberedName> returnedNames = mediumService.getNamesList();

    verify(mockMediumRepository, times(1))
        .findMediaNames();

    assertThat(returnedNames).isEqualTo(names);
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


}
