package com.aniamadej.sublokator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import com.aniamadej.sublokator.ErrorMessageSource;
import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.ReadingRepository;
import com.aniamadej.sublokator.service.MediumMeterService;
import com.aniamadej.sublokator.util.Mappings;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
  private MediumConnectionRepository mediumConnectionRepository;

  @Autowired
  private MessageSource messageSource;


  @Autowired
  EntityManager entityManager;

  MediumMeter activeResettableMediumMeter;
  MediumMeter inactiveResettableMediumMeter;
  MediumMeter activeNotResettableMediumMeter;

  @BeforeAll
  public void init() {

    MediumConnection mediumConnection = new MediumConnection("medium");

    activeResettableMediumMeter = new MediumMeter();
    activeResettableMediumMeter.setNumber("activeResettable");
    activeResettableMediumMeter.setResettable(true);
    activeResettableMediumMeter.setActiveSince(LocalDate.now().minusDays(1));
    activeResettableMediumMeter.setUnitName("kwh");

    inactiveResettableMediumMeter = new MediumMeter();
    inactiveResettableMediumMeter.setNumber("inactiveResettable");
    inactiveResettableMediumMeter.setActiveSince(LocalDate.now().minusDays(1));
    inactiveResettableMediumMeter.setActiveUntil(LocalDate.now());
    inactiveResettableMediumMeter.setResettable(true);
    inactiveResettableMediumMeter.setUnitName("m3");

    activeNotResettableMediumMeter = new MediumMeter();
    activeNotResettableMediumMeter.setNumber("inactiveResettable");
    activeNotResettableMediumMeter.setActiveSince(LocalDate.now().minusDays(1));
    activeNotResettableMediumMeter.setResettable(false);
    activeNotResettableMediumMeter.setUnitName("unit");

    activeResettableMediumMeter.setMediumConnection(mediumConnection);
    inactiveResettableMediumMeter.setMediumConnection(mediumConnection);
    activeNotResettableMediumMeter.setMediumConnection(mediumConnection);

    Reading reading1 = new Reading();
    reading1.setDate(LocalDate.now().minusDays(1));
    reading1.setReading(12.);

    Reading reading2 = new Reading();
    reading2.setDate(LocalDate.now());
    reading2.setReading(13.);

    reading1.setMediumMeter(activeResettableMediumMeter);
    reading2.setMediumMeter(activeResettableMediumMeter);

    mediumConnectionRepository.save(mediumConnection);
  }

  @Test
  @DisplayName("http get request should show active resettable medium meter "
      + "webpage with deactivation form and reset form")
  public void httpGetShowsActiveResettableMeterPage() {
    String url =
        "http://localhost:" + port + Mappings.METER_PAGE + "/"
            + activeResettableMediumMeter
            .getId();

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(url, String.class);

    Document webPage = Jsoup.parse(response.getBody());
    assertThat(webPage.select("#reactivateForm")).isEmpty();

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
        Mappings.METER_PAGE + "/" + activeResettableMediumMeter.getId()
            + Mappings.DEACTIVATE,
        webPage.select("#deactivateForm").attr("action"));

    assertEquals(getMessage("page.activeUntil"),
        webPage.select("#activeUntilInputLabel").text());
    assertEquals("date", webPage.select("#activeUntilInput").attr("type"));

    assertEquals("submit", webPage.select("#deactivateButton").attr("type"));
    assertEquals(getMessage("page.deactivate"),
        webPage.select("#deactivateButton").text());


    assertEquals(
        Mappings.METER_PAGE + "/" + activeResettableMediumMeter.getId()
            + Mappings.RESET,
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
        readingRepository
            .findByMediumMeterId(activeResettableMediumMeter.getId());
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

    assertThat(webPage.select("#actuveUntilLabel")).isEmpty();
    assertThat(webPage.select("#actuveUntil")).isEmpty();

  }

  @Test
  @DisplayName("http get request should return medium meter webPage "
      + "with reactivation button instead of deactivation")
  public void httpGetShowsMeterPageWithReactivationForm() {
    String url =
        "http://localhost:" + port + Mappings.METER_PAGE + "/"
            + inactiveResettableMediumMeter.getId();

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(url, String.class);

    Document webPage = Jsoup.parse(response.getBody());
    assertThat(webPage.select("#deactivateForm")).isEmpty();

    assertEquals(getMessage("page.activeUntil"),
        webPage.select("#activeUntilLabel").text());

    assertEquals(inactiveResettableMediumMeter.getActiveUntil().toString(),
        webPage.select("#activeUntil").text());

    Elements reactivateForm = webPage.select("#reactivateForm");
    Element reactivateButton =
        reactivateForm.select("button").first();
    assertEquals("reactivateButton", reactivateButton.id());

    assertEquals(
        Mappings.METER_PAGE + "/" + inactiveResettableMediumMeter.getId()
            + Mappings.REACTIVATE, reactivateForm.attr("action"));

    assertEquals(
        "submit", reactivateButton.attr("type"));

    assertEquals(
        getMessage("page.cancelDeactivation"), reactivateButton.text());

    assertEquals(
        Mappings.METER_PAGE + "/" + inactiveResettableMediumMeter.getId()
            + Mappings.RESET,
        webPage.select("#resetForm").attr("action"));

    assertEquals(getMessage("page.resetDate"),
        webPage.select("#resetLabel").text());
    assertEquals("date", webPage.select("#resetDateInput").attr("type"));

    assertEquals("submit", webPage.select("#resetButton").attr("type"));
    assertEquals(getMessage("page.reset"),
        webPage.select("#resetButton").text());
  }

  @Test
  @DisplayName("http get request should return medium meter webPage "
      + "with activation button and without reset button ")
  public void httpGetShowsMeterPageWithoutResetForm() {
    String url =
        "http://localhost:" + port + Mappings.METER_PAGE + "/"
            + activeNotResettableMediumMeter.getId();

    ResponseEntity<String> response =
        testRestTemplate.getForEntity(url, String.class);

    Document webPage = Jsoup.parse(response.getBody());
    assertThat(webPage.select("#reactivateForm")).isEmpty();
    
    Elements deactivateForm = webPage.select("#deactivateForm");
    Element deactivateButton =
        deactivateForm.select("button").first();
    assertEquals("deactivateButton", deactivateButton.id());

    assertEquals(
        Mappings.METER_PAGE + "/" + activeNotResettableMediumMeter.getId()
            + Mappings.DEACTIVATE, deactivateForm.attr("action"));

    assertEquals(
        "submit", deactivateButton.attr("type"));

    assertEquals(
        getMessage("page.deactivate"), deactivateButton.text());

    assertThat(webPage.select("#resetForm").attr("action")).isEmpty();


    assertThat(webPage.select("#actuveUntilLabel")).isEmpty();
    assertThat(webPage.select("#actuveUntil")).isEmpty();

  }

  private String getMessage(String messageCode) {
    return messageSource
        .getMessage(messageCode, null,
            LocaleContextHolder.getLocale());
  }


}