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
import com.aniamadej.sublokator.testService.RequestSenderService;
import com.aniamadej.sublokator.util.Mappings;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MetersControllerE2ETests {

  @LocalServerPort
  private int port;

  @Autowired
  private RequestSenderService requestSenderService;

  @Autowired
  private ReadingRepository readingRepository;

  @Autowired
  private MediumMeterRepository mediumMeterRepository;

  @Autowired
  private ErrorMessageSource errorMessageSource;

  @Autowired
  private MediumConnectionRepository mediumConnectionRepository;

  @Autowired
  private MessageSource messageSource;


  private MediumMeter activeResettableMediumMeter;
  private MediumMeter inactiveResettableMediumMeter;
  private MediumMeter activeNotResettableMediumMeter;
  private MediumMeter inactiveNotResettableMediumMeter;
  private String urlPrefix;

  @BeforeAll
  public void init() {

    urlPrefix = "http://localhost:" + port;


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
    activeNotResettableMediumMeter.setNumber("activeNotResettable");
    activeNotResettableMediumMeter.setActiveSince(LocalDate.now().minusDays(1));
    activeNotResettableMediumMeter.setResettable(false);
    activeNotResettableMediumMeter.setUnitName("unit");

    inactiveNotResettableMediumMeter = new MediumMeter();
    inactiveNotResettableMediumMeter.setNumber("inactiveNotResettable");
    inactiveNotResettableMediumMeter
        .setActiveSince(LocalDate.now().minusDays(1));
    inactiveNotResettableMediumMeter.setActiveUntil(LocalDate.now());
    inactiveNotResettableMediumMeter.setResettable(false);
    inactiveNotResettableMediumMeter.setUnitName("some-unit");


    activeResettableMediumMeter.setMediumConnection(mediumConnection);
    inactiveResettableMediumMeter.setMediumConnection(mediumConnection);
    activeNotResettableMediumMeter.setMediumConnection(mediumConnection);
    inactiveNotResettableMediumMeter.setMediumConnection(mediumConnection);

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
      + "webPage with deactivation form and reset form")
  public void httpGet_ShowsActiveResettableMeterPage() {
    String url =
        "http://localhost:" + port + Mappings.METER_PAGE + "/"
            + activeResettableMediumMeter
            .getId();

    ResponseEntity<String> response = requestSenderService.sendGet(url);

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

    readings.forEach(reading -> assertTrue(readingsRows.stream().anyMatch(tr ->
        tr.select("td").get(0).text().equals(reading.getDate().toString())
            && tr.select("td").get(1).text()
            .equals(Double.toString(reading.getReading()))
            && tr.select("td > form").attr("method").equals("post")
            && tr.select("td > form").attr("action").equals(
            Mappings.READING_PAGE + "/" + reading.getId()
                + Mappings.DELETE)
            && tr.select("td > form > button").text()
            .equals(getMessage("page.delete"))
    )));

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
  public void httpGet_ShowsMeterPageWithReactivationForm() {
    String url =
        "http://localhost:" + port + Mappings.METER_PAGE + "/"
            + inactiveResettableMediumMeter.getId();

    ResponseEntity<String> response = requestSenderService.sendGet(url);

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
  public void httpGet_ShowsMeterPageWithoutResetForm() {
    this.urlPrefix = "http://localhost:" + port;
    String url =
        this.urlPrefix + Mappings.METER_PAGE + "/"
            + activeNotResettableMediumMeter.getId();

    ResponseEntity<String> response = requestSenderService.sendGet(url);

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

  @Test
  @DisplayName("http get request should return medium meter webPage "
      + "with reactivation button and without reset button ")
  public void httpGetShowsMeterPageWithoutResetFormWithReactivationForm() {
    String url =
        "http://localhost:" + port + Mappings.METER_PAGE + "/"
            + inactiveNotResettableMediumMeter.getId();

    ResponseEntity<String> response = requestSenderService.sendGet(url);

    Document webPage = Jsoup.parse(response.getBody());
    assertThat(webPage.select("#deactivateForm")).isEmpty();

    Elements reactivateForm = webPage.select("#reactivateForm");
    Element reactivateButton =
        reactivateForm.select("button").first();
    assertEquals("reactivateButton", reactivateButton.id());

    assertEquals(
        Mappings.METER_PAGE + "/" + inactiveNotResettableMediumMeter.getId()
            + Mappings.REACTIVATE, reactivateForm.attr("action"));

    assertEquals(
        "submit", reactivateButton.attr("type"));

    assertEquals(
        getMessage("page.cancelDeactivation"), reactivateButton.text());

    assertEquals(getMessage("page.activeUntil"),
        webPage.select("#activeUntilLabel").text());

    assertEquals(inactiveNotResettableMediumMeter.getActiveUntil().toString(),
        webPage.select("#activeUntil").text());

    assertThat(webPage.select("#resetForm")).isEmpty();

  }

  @Test
  @DisplayName("post request sent to reading adding meter page should "
      + "add new reading and redirect to same page with this reading visible")
  public void httpPost_addsReadingToMeter() {

    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());


    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();
    ReadingBasics lastReading =
        getLatestReading(activeResettableMediumMeter);

    String readingDate = lastReading.getDate().plusDays(1).toString();
    String readingValue = Double.toString(lastReading.getReading() + 1);

    formInputs.add("date", readingDate);
    formInputs.add("reading", readingValue);

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    Long newReadingId = getLastInsertedReadingId(activeResettableMediumMeter);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings + 1, readingsRows.size());

    assertTrue(readingsRows.stream().anyMatch(tr ->
        tr.select("td").get(0).text().equals(readingDate)
            && tr.select("td").get(1).text()
            .equals(readingValue)
            && tr.select("td > form").attr("method").equals("post")
            && tr.select("td > form").attr("action").equals(
            Mappings.READING_PAGE + "/" + newReadingId
                + Mappings.DELETE)
            && tr.select("td > form > button").text()
            .equals(getMessage("page.delete"))
    ));

  }

  @Test
  @DisplayName("post request sent to reading adding meter page should NOT"
      + "add new reading with null form input values and should show errors")
  public void httpPost_showsErrorNullReadingFormValues() {

    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());


    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    formInputs.add("date", null);
    formInputs.add("reading", null);

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings, readingsRows.size());

    assertThat(webPage.select("#newReadingError").text())
        .contains(errorMessageSource.getMessage("error.number"));

    assertThat(webPage.select("#newReadingDateError").text())
        .contains(errorMessageSource.getMessage("error.date"));


  }

  @Test
  @DisplayName("post request sent to reading adding meter page should NOT"
      + "add new reading with empty form input values and should show errors")
  public void httpPost_showsErrorEmptyReadingFormValues() {


    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());


    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    formInputs.add("date", "");
    formInputs.add("reading", "");

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings, readingsRows.size());

    assertThat(webPage.select("#newReadingError").text())
        .contains(errorMessageSource.getMessage("error.number"));

    assertThat(webPage.select("#newReadingDateError").text())
        .contains(errorMessageSource.getMessage("error.date"));


  }

  @Test
  @DisplayName("post request sent to reading adding meter page should NOT"
      + "add new reading with blank form input values and should show errors")
  public void httpPost_showsErrorBlankReadingFormValues() {

    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());


    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    formInputs.add("date", " ");
    formInputs.add("reading", " ");

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings, readingsRows.size());

    assertThat(webPage.select("#newReadingError").text())
        .contains(errorMessageSource.getMessage("error.number"));

    assertThat(webPage.select("#newReadingDateError").text())
        .contains(errorMessageSource.getMessage("error.date"));


  }

  @Test
  @DisplayName("post request sent to reading adding meter page should NOT"
      + "add new reading with string (not parsable)  form input values "
      + "and should show errors")
  public void httpPost_showsErrorNotParsableReadingFormValues() {

    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());


    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    formInputs.add("date", "wrong input");
    formInputs.add("reading", "wrong input");

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings, readingsRows.size());

    assertThat(webPage.select("#newReadingError").text())
        .contains(errorMessageSource.getMessage("error.number"));

    assertThat(webPage.select("#newReadingDateError").text())
        .contains(errorMessageSource.getMessage("error.date"));
  }

  @Test
  @DisplayName("post request sent to reading adding meter page should "
      + "NOT add new reading if it is smaller than before reading value")
  public void httpPost_showsErrorReadingValueSmallerThanBefore() {

    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());

    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();
    ReadingBasics lastReading =
        getLatestReading(activeResettableMediumMeter);

    Reading beforeReading = new Reading();
    beforeReading.setDate(lastReading.getDate().plusDays(1));
    beforeReading.setReading(lastReading.getReading() + 2);
    activeResettableMediumMeter.addReading(beforeReading);
    activeResettableMediumMeter =
        mediumMeterRepository.save(activeResettableMediumMeter);

    String readingDate = beforeReading.getDate().plusDays(1).toString();
    String readingValue = Double.toString(beforeReading.getReading() - 1);

    formInputs.add("date", readingDate);
    formInputs.add("reading", readingValue);

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings + 1, readingsRows.size());

    assertEquals(errorMessageSource.getMessage("error.wrongReadingValue"),
        webPage.select("#error").text());


  }

  @Test
  @DisplayName("post request sent to reading adding meter page should "
      + "NOT add new reading if it is bigger than  than next reading value")
  public void httpPost_showsErrorReadingValueBiggerThanAfter() {


    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());


    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId() + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();
    ReadingBasics lastReading =
        getLatestReading(activeResettableMediumMeter);

    Reading reading = new Reading();
    reading.setDate(lastReading.getDate().plusDays(2));
    reading.setReading(lastReading.getReading() + 2);
    activeResettableMediumMeter.addReading(reading);
    activeResettableMediumMeter =
        mediumMeterRepository.save(activeResettableMediumMeter);

    String readingDate = reading.getDate().minusDays(1).toString();
    String readingValue = Double.toString(reading.getReading() + 1);

    formInputs.add("date", readingDate);
    formInputs.add("reading", readingValue);


    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);


    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings + 1, readingsRows.size());

    assertEquals(errorMessageSource.getMessage("error.wrongReadingValue"),
        webPage.select("#error").text());

  }

  @Test
  @DisplayName("post request sent to reading adding meter page should "
      + "NOT add new reading if it is before meter activation date")
  public void httpPost_showsErrorReadingBeforeMeterActivation() {

    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());


    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    String readingDate =
        activeResettableMediumMeter.getActiveSince().minusDays(1).toString();
    String readingValue = Double.toString(2L);

    formInputs.add("date", readingDate);
    formInputs.add("reading", readingValue);

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings, readingsRows.size());

    assertEquals(
        errorMessageSource.getMessage("error.readingBeforeActivation"),
        webPage.select("#error").text());

  }

  @Test
  @DisplayName("post request sent to reading adding meter page should "
      + "NOT add new reading if it is after meter deactivation date")
  public void httpPost_showsErrorReadingAfterMeterDeactivation() {


    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(inactiveResettableMediumMeter.getId());


    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + inactiveResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + inactiveResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    String readingDate =
        inactiveResettableMediumMeter.getActiveUntil().plusDays(1).toString();
    String readingValue = Double.toString(2L);

    formInputs.add("date", readingDate);
    formInputs.add("reading", readingValue);

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings, readingsRows.size());

    assertEquals(
        errorMessageSource.getMessage("error.readingAfterDeactivation"),
        webPage.select("#error").text());

  }

  @Test
  @DisplayName("post request sent to reading adding meter page should "
      + "NOT add new reading at duplicated reading date")
  public void httpPost_showsErrorReadingDateDuplicated() {

    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());

    ReadingBasics lastReading =
        getLatestReading(activeResettableMediumMeter);

    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    String readingDate = lastReading.getDate().toString();
    String readingValue = Double.toString(32L);

    formInputs.add("date", readingDate);
    formInputs.add("reading", readingValue);

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings, readingsRows.size());

    assertEquals(
        errorMessageSource.getMessage("error.duplicateReading"),
        webPage.select("#error").text());
  }

  @Test
  @DisplayName("post request sent to reading adding meter page should "
      + "add new reading bigger than previous and smaller than next "
      + "if next is zero")
  public void httpPost_addsNewReadingBetweenSmallerAndZero() {

    int initialNumberOfReadings =
        readingRepository
            .countAllByMediumMeterId(activeResettableMediumMeter.getId());

    ReadingBasics lastReading =
        getLatestReading(activeResettableMediumMeter);

    Reading zeroReading = new Reading();
    zeroReading.setDate(lastReading.getDate().plusDays(2));
    zeroReading.setReading(0.);
    activeResettableMediumMeter.addReading(zeroReading);
    activeResettableMediumMeter =
        mediumMeterRepository.save(activeResettableMediumMeter);

    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    String readingDate = lastReading.getDate().plusDays(1).toString();
    String readingValue = Double.toString(lastReading.getReading() + 1);

    formInputs.add("date", readingDate);
    formInputs.add("reading", readingValue);

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");

    assertEquals(initialNumberOfReadings + 2, readingsRows.size());

    Long newReadingId = getLastInsertedReadingId(activeResettableMediumMeter);

    assertTrue(readingsRows.stream().anyMatch(tr ->
        tr.select("td").get(0).text().equals(readingDate)
            && tr.select("td").get(1).text()
            .equals(readingValue)
            && tr.select("td > form").attr("method").equals("post")
            && tr.select("td > form").attr("action").equals(
            Mappings.READING_PAGE + "/" + newReadingId
                + Mappings.DELETE)
            && tr.select("td > form > button").text()
            .equals(getMessage("page.delete"))
    ));

  }

  @Test
  @DisplayName("post request sent to reading adding meter page should "
      + "NOT add any new reading if meter not exists")
  public void httpPost_showsErrorMeterNotExists() {

    Long mediumMeterId = 19L;

    while (mediumMeterRepository.existsById(mediumMeterId)) {
      mediumMeterId++;
    }

    Long initialNumberOfAllReadings = mediumMeterRepository.count();

    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + mediumMeterId
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + mediumMeterId;

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    String readingDate = LocalDate.now().plusDays(14).toString();
    String readingValue = Double.toString(32L);

    formInputs.add("date", readingDate);
    formInputs.add("reading", readingValue);

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    assertEquals(
        errorMessageSource.getMessage("error.meterNotExists"),
        webPage.select("#error").text());

    assertEquals(mediumMeterRepository.count(), initialNumberOfAllReadings);

    assertEquals(getMessage("page.mediaConnections"),
        webPage.select("#pageHeader").text());
  }

  @Test
  @DisplayName("post request sent to reading adding meter page should NOT"
      + "add new reading which has negative value and should show error")
  public void httpPost_showsErrorNegativeReading() {


    int initialNumberOfReadings = readingRepository
        .countAllByMediumMeterId(activeResettableMediumMeter.getId());

    String destinationUrl =
        urlPrefix + Mappings.METER_PAGE + "/" + activeResettableMediumMeter
            .getId()
            + Mappings.READING_ADD_SUBPAGE;
    String refererUrl = urlPrefix + Mappings.METER_PAGE + "/"
        + activeResettableMediumMeter.getId();

    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();

    ReadingBasics lastReading = getLatestReading(activeResettableMediumMeter);

    formInputs.add("date", lastReading.getDate().plusDays(1).toString());
    formInputs.add("reading", "-1");

    ResponseEntity<String> response =
        requestSenderService.sendPost(destinationUrl, refererUrl, formInputs);

    assertEquals(200, response.getStatusCodeValue());

    Document webPage = Jsoup.parse(response.getBody());

    Elements readingsRows = webPage.select("#readingsTable > tbody > tr");
    assertEquals(initialNumberOfReadings, readingsRows.size());

    assertThat(webPage.select("#newReadingError").text())
        .contains(errorMessageSource.getMessage("error.onlyPositive"));

  }


  private Long getLastInsertedReadingId(MediumMeter meter) {
    return readingRepository
        .findByMediumMeterId(meter.getId()).stream().map(
            ReadingBasics::getId).max(Long::compareTo).get();
  }

  private ReadingBasics getLatestReading(MediumMeter meter) {
    return readingRepository.findByMediumMeterId(meter.getId()).stream()
        .min((r1, r2) -> r2.getDate().compareTo(r1.getDate())).get();
  }

  private String getMessage(String messageCode) {
    return messageSource
        .getMessage(messageCode, null,
            LocaleContextHolder.getLocale());
  }


}