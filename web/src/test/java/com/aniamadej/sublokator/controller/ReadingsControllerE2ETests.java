package com.aniamadej.sublokator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


import com.aniamadej.sublokator.ErrorMessageSource;
import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.dto.input.ReadingForm;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.model.Reading;
import com.aniamadej.sublokator.repository.ReadingRepository;
import com.aniamadej.sublokator.service.MediumMeterService;
import com.aniamadej.sublokator.testService.RequestSenderService;
import com.aniamadej.sublokator.util.Mappings;
import java.time.LocalDate;
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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReadingsControllerE2ETests {

  @LocalServerPort
  private int port;

  @Autowired
  private ReadingRepository readingRepository;

  @Autowired
  private MediumMeterService mediumMeterService;

  @Autowired
  private ErrorMessageSource errorMessageSource;

  @Autowired
  private RequestSenderService requestSenderService;

  private String urlPrefix;

  private MediumConnection mediumConnection;
  private MediumMeter mediumMeter;
  private Reading reading;

  @BeforeAll
  public void init() {

    urlPrefix = "http://localhost:" + port;

    mediumConnection = new MediumConnection();
    mediumMeter = new MediumMeter();
    mediumMeter.setNumber("123");
    mediumMeter.setUnitName("kwh");
    mediumMeter.setResettable(true);
    mediumMeter.setActiveSince(LocalDate.now().minusDays(30));
    reading = new Reading();
    reading.setDate(LocalDate.now().minusDays(30));
    reading.setReading(12.0);
    reading.setMediumMeter(mediumMeter);
    mediumMeter.setMediumConnection(mediumConnection);
    reading = readingRepository.save(reading);

  }

  @Test
  @DisplayName("should delete reading with provided id which is NOT first "
      + "reading of meter and is NOT zero (and also NOT last zero) reading")
  public void httpPost_deletesReading() {

    // adding new reading to db
    String readingDate = LocalDate.now().minusDays(20).toString();
    String readingValue = Double.toString(13.0);
    mediumMeterService
        .addReading(mediumMeter.getId(),
            new ReadingForm(readingDate, readingValue));

    Long readingId =
        readingRepository.findByMediumMeterId(mediumMeter.getId()).stream()
            .map(ReadingBasics::getId).max(Long::compareTo)
            .orElseThrow(IllegalStateException::new);

    // new reading is in db
    assertTrue(readingRepository.existsById(readingId));

    // sending post request
    String destinationUrl =
        urlPrefix + Mappings.READING_PAGE + "/" + readingId
            + Mappings.DELETE;

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl);

    Elements readingsRows = readingsRows(response);

    assertEquals(200, response.getStatusCodeValue());


    // reading does not exist in db
    assertFalse(readingRepository.existsById(readingId));


    // reading does not show on result webpage
    assertTrue(
        readingsRows.stream().noneMatch(tr -> {
          Elements tds = tr.select("td");
          return tds.get(0).text().equals(readingDate)
              &&
              tds.get(1).text().equals(readingValue);
        }));


  }

  @Test
  @DisplayName("should delete reading with provided id which is NOT first "
      + "reading of meter, IS last on the list and IS zero")
  public void httpPost_deletesLastZeroReading() {

    // adding new ZERO reading to db
    String readingDate = LocalDate.now().minusDays(19).toString();
    mediumMeterService.reset(mediumMeter.getId(), readingDate);

    Long readingId =
        readingRepository.findByMediumMeterId(mediumMeter.getId()).stream()
            .map(ReadingBasics::getId).max(Long::compareTo)
            .orElseThrow(IllegalStateException::new);

    // new reading is in db
    assertTrue(readingRepository.existsById(readingId));

    // sending post request
    String destinationUrl =
        urlPrefix + Mappings.READING_PAGE + "/" + readingId
            + Mappings.DELETE;

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl);

    assertEquals(200, response.getStatusCodeValue());

    // reading does not exist in db
    assertFalse(readingRepository.existsById(readingId));


    // reading does not show on result webpage
    Elements readingsRows = readingsRows(response);
    assertTrue(
        readingsRows.stream().noneMatch(tr -> {
          Elements tds = tr.select("td");
          return tds.get(0).text().equals(readingDate)
              &&
              tds.get(1).text().equals(Double.toString(0.0));
        }));

  }


  @Test
  @DisplayName("should NOT delete reading with provided id which is NOT first "
      + "reading of meter, is NOT last and IS zero")
  public void httpPost_doesntDeleteNotLastZeroReading() {

    // adding new ZERO reading to db
    String readingDate = LocalDate.now().minusDays(19).toString();
    mediumMeterService.reset(mediumMeter.getId(), readingDate);

    Long readingId =
        readingRepository.findByMediumMeterId(mediumMeter.getId()).stream()
            .map(ReadingBasics::getId).max(Long::compareTo)
            .orElseThrow(IllegalStateException::new);

    // adding new reading to db after zero
    String reading1Date = LocalDate.now().minusDays(18).toString();
    String reading1Value = Double.toString(13.0);
    mediumMeterService
        .addReading(mediumMeter.getId(),
            new ReadingForm(reading1Date, reading1Value));

    // zero reading is in db
    assertTrue(readingRepository.existsById(readingId));


    // Setting referer as request header because ExceptionHandlingController
    // needs it to redirect to same page -> this is address of page which usually
    // sends post request
    String referrerUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + mediumMeter
            .getId();

    String destinationUrl =
        urlPrefix + Mappings.READING_PAGE + "/" + readingId
            + "/delete";

    // sending post request
    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, referrerUrl);

    assertEquals(200, response.getStatusCodeValue());


    // zero reading still exists in db
    assertTrue(readingRepository.existsById(readingId));

    // reading shows on result webPage
    Elements readingsRows = readingsRows(response);
    assertTrue(
        readingsRows.stream().anyMatch(tr -> {
          Elements tds = tr.select("td");
          return tds.get(0).text().equals(readingDate)
              &&
              tds.get(1).text().equals(Double.toString(0.0));
        }));

    // error paragraph on result page shows error
    assertThat(errorParagraph(response).text()
        .contains(errorMessageSource.getMessage("error.zeroDelete")));

  }

  @Test
  @DisplayName("should NOT delete first reading of meter")
  public void httpPost_doesntDeleteFirstReading() {

    Long readingId = reading.getId();

    // Setting referer as request header because ExceptionHandlingController
    // needs it to redirect to same page -> this is address of page which usually
    // sends post request
    String referrerUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + mediumMeter
            .getId();

    String destinationUrl =
        urlPrefix + Mappings.READING_PAGE + "/" + readingId
            + "/delete";

    // sending post request
    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, referrerUrl);
    assertEquals(200, response.getStatusCodeValue());


    // first reading still exists in db
    assertTrue(readingRepository.existsById(readingId));

    // error paragraph on result page shows error
    assertThat(errorParagraph(response).text()
        .contains(errorMessageSource.getMessage("error.firstDelete")));

    // first reading shows on result webPage
    Elements readingsRows = readingsRows(response);
    assertTrue(
        readingsRows.stream().anyMatch(tr -> {
          Elements tds = tr.select("td");
          return tds.get(0).text().equals(reading.getDate().toString())
              &&
              tds.get(1).text().equals(Double.toString(reading.getReading()));
        }));

  }

  @Test
  @DisplayName("should redirect to main page as reading id does not exist")
  public void httpPost_redirectsToMainPageReadingNotExists() {

    Long readingId = Long.MAX_VALUE;

    // this reading does not exist
    assertFalse(readingRepository.existsById(readingId));

    // sending post request
    String destinationUrl =
        urlPrefix + Mappings.READING_PAGE + "/" + readingId
            + "/delete";
    ResponseEntity<String> response = requestSenderService.sendPost(destinationUrl);

    assertEquals(200, response.getStatusCodeValue());

    // error paragraph on result page shows error
    assertThat(errorParagraph(response).text()
        .contains(errorMessageSource.getMessage("error.noId")));
  }

  private Element errorParagraph(ResponseEntity<String> response) {
    return Jsoup.parse(response.getBody()).getElementById("error");
  }

  private Elements readingsRows(ResponseEntity<String> response) {
    Document webPage = Jsoup.parse(response.getBody());
    return webPage.getElementById("readingsTable").select("tbody").select("tr");
  }



}

