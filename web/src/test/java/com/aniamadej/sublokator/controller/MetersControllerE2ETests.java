package com.aniamadej.sublokator.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import com.aniamadej.sublokator.ErrorMessageSource;
import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.ReadingRepository;
import com.aniamadej.sublokator.service.MediumMeterService;
import com.aniamadej.sublokator.util.Mappings;
import java.time.LocalDate;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MetersControllerE2ETests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  private ReadingRepository readingRepository;

  @Autowired
  private MediumMeterRepository mediumMeterRepository;

  @Autowired
  private MediumMeterService mediumMeterService;

  @Autowired
  private ErrorMessageSource errorMessageSource;

  @Autowired
  private MessageSource messageSource;

  private MediumMeter activeResettableMediumMeter;

  @BeforeAll
  public void init() {

    MediumConnection mediumConnection = new MediumConnection();
    activeResettableMediumMeter = new MediumMeter();
    activeResettableMediumMeter.setNumber("123");
    activeResettableMediumMeter.setUnitName("kwh");
    activeResettableMediumMeter.setResettable(true);
    activeResettableMediumMeter.setActiveSince(LocalDate.now().minusDays(30));
    Reading reading = new Reading();
    reading.setDate(LocalDate.now().minusDays(30));
    reading.setReading(12.0);
    reading.setMediumMeter(activeResettableMediumMeter);
    Reading reading2 = new Reading();
    reading2.setMediumMeter(activeResettableMediumMeter);
    reading2.setReading(13.0);
    reading2.setDate(LocalDate.now().minusDays(29));
    activeResettableMediumMeter.addReading(reading2);
    mediumMeterRepository.save(activeResettableMediumMeter);

    activeResettableMediumMeter.setMediumConnection(mediumConnection);
    activeResettableMediumMeter.addReading(reading);
    activeResettableMediumMeter.addReading(reading2);
    activeResettableMediumMeter = mediumMeterRepository.save(
        activeResettableMediumMeter);


  }

  @Test
  @DisplayName("http get request should medium meter webpage "
      + "with deactivation form and reset form")
  public void httpGetShowsMeterPage() {
    String url =
        "http://localhost:" + port + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId();

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(url, String.class);

    Document webPage = Jsoup.parse(response.getBody());

    assertEquals(getMessage("page.mediumMeter"),
        webPage.select("#mediumMeterHeader").text());


    assertEquals(getMessage("page.meterNumber"),
        webPage.select("#meterNumberLabel").text());

    assertEquals(activeResettableMediumMeter.getNumber(),
        webPage.select("#meterNumber").text());

    assertEquals(getMessage("page.meterUnit"),
        webPage.select("#meterUnitLabel").text());

    assertEquals(activeResettableMediumMeter.getUnitName(),
        webPage.select("#meterUnit").text());

    assertEquals(getMessage("page.activeSince"),
        webPage.select("#activeSinceLabel").text());

    assertEquals(activeResettableMediumMeter.getActiveSince().toString(),
        webPage.select("#activeSince").text());


    assertEquals(
        Mappings.METER_PAGE + "/" + activeResettableMediumMeter.getId() + Mappings.DEACTIVATE,
        webPage.select("#deactivateForm").attr("action"));

    assertEquals(getMessage("page.activeUntil"),
        webPage.select("#activeUntilInputLabel").text());
    assertEquals("date", webPage.select("#activeUntilInput").attr("type"));

    assertEquals("submit", webPage.select("#deactivateButton").attr("type"));
    assertEquals(getMessage("page.deactivate"),
        webPage.select("#deactivateButton").text());


    assertEquals(
        Mappings.METER_PAGE + "/" + activeResettableMediumMeter.getId() + Mappings.RESET,
        webPage.select("#resetForm").attr("action"));

    assertEquals(getMessage("page.resetDate"),
        webPage.select("#resetLabel").text());
    assertEquals("date", webPage.select("#resetDateInput").attr("type"));

    assertEquals("submit", webPage.select("#resetButton").attr("type"));
    assertEquals(getMessage("page.reset"),
        webPage.select("#resetButton").text());

    assertEquals(getMessage("page.readings"),
        webPage.select("#readingsHeader").text());


    assertEquals(getMessage("page.date"),
        webPage.select("#dateHeader").text());

    assertEquals(getMessage("page.reading"),
        webPage.select("#readingHeader").text());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");

    List<ReadingBasics> readings =
        readingRepository.findByMediumMeterId(activeResettableMediumMeter.getId());
    assertEquals(readings.size(),
        readingsRows.size());

    readings.forEach(reading -> {
      assertTrue(readingsRows.stream().anyMatch(tr ->
          tr.select("td").get(0).text().equals(reading.getDate().toString())
              && tr.select("td").get(1).text()
              .equals(Double.toString(reading.getReading()))
              && tr.select("td > form").attr("method").equals("post")
              && tr.select("td > form").attr("action").equals(
              Mappings.READING_PAGE + "/" + reading.getId()
                  + Mappings.DELETE)));
    });

    assertEquals(getMessage("page.addReading"),
        webPage.select("#addReadingHeader").text());

    assertEquals("post", webPage.select("#newReadingForm").attr("method"));
    assertEquals(Mappings.METER_PAGE + "/" + activeResettableMediumMeter.getId()
            + Mappings.READING_ADD_SUBPAGE,
        webPage.select("#newReadingForm").attr("action"));

    assertEquals(getMessage("page.date"),
        webPage.select("#newReadingDateLabel").text());

    assertEquals("date", webPage.select("#newReadingDateInput").attr("type"));

    assertEquals(getMessage("page.reading"),
        webPage.select("#newReadingLabel").text());

    assertEquals("number", webPage.select("#newReadingInput").attr("type"));

    assertEquals(getMessage("page.addButton"),
        webPage.select("#newReadingForm > div > button").text());

    assertEquals("submit",
        webPage.select("#newReadingForm > div > button").attr("type"));

  }

  private String getMessage(String messageCode) {
    return messageSource
        .getMessage(messageCode, null,
            LocaleContextHolder.getLocale());
  }


}