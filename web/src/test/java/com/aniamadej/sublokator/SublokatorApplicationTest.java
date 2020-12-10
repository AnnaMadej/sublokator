package com.aniamadej.sublokator;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ResourceBundleMessageSource;

@Slf4j
@SpringBootTest
class SublokatorApplicationTest {

    @Autowired
    ResourceBundleMessageSource messageSource;

    @Test
    public void contextLoads(){
    }

    @Test
    public void getsMessageFromMessageSource(){
//       log.info("{}", messageSource.getMessage("page.addReading", null, Locale.ENGLISH));
    }
}