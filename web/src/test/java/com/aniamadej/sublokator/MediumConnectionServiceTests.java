package com.aniamadej.sublokator;

import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MediumConnectionServiceTests {

    @Autowired
    private MediumConnectionRepository mediumConnectionRepository;

    @Test
    void contextLoads() {
    }

}
