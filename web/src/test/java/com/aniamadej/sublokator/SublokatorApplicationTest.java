package com.aniamadej.sublokator;

import com.aniamadej.sublokator.repository.MediumMeterRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ResourceBundleMessageSource;

@Slf4j
@SpringBootTest
class SublokatorApplicationTest {

  @Autowired
  private ResourceBundleMessageSource messageSource;

  @Autowired
  private MediumMeterRepository mediumMeterRepository;

  @Test
  public void contextLoads() {
  }


}
