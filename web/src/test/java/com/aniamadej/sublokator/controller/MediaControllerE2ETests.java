package com.aniamadej.sublokator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import com.aniamadej.sublokator.ErrorMessageSource;
import com.aniamadej.sublokator.dto.NumberedName;
import com.aniamadej.sublokator.dto.ReadingBasics;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.repository.ReadingRepository;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.testService.RequestSenderService;
import com.aniamadej.sublokator.util.Attributes;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MediaControllerE2ETests {

  @LocalServerPort
  private int port;

  @Autowired
  private MediumConnectionService mediumConnectionService;

  @Autowired
  private
  MediumConnectionRepository mediumConnectionRepository;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private ErrorMessageSource errorsMessageSource;

  @Autowired
  private MediumMeterRepository mediumMeterRepository;

  @Autowired
  private ReadingRepository readingRepository;

  @Autowired
  private RequestSenderService requestSenderService;

  private String urlPrefix;


  private MediumConnection medium1;
  private MediumConnection medium2;

  private MediumMeter mediumMeter1;
  private MediumMeter mediumMeter2;
  private MediumMeter mediumMeter3;
  private MediumMeter mediumMeter4;

  @BeforeAll
  public void init() {

    urlPrefix = "http://localhost:" + port;

    medium1 = new MediumConnection();
    medium1.setMediumName("Gaz");

    mediumMeter1 = new MediumMeter();
    mediumMeter1.setNumber("Active, Resettable");
    mediumMeter1.setActiveSince(LocalDate.now());
    mediumMeter1.setResettable(true);

    mediumMeter2 = new MediumMeter();
    mediumMeter2.setNumber("Active, Not resettable");
    mediumMeter2.setActiveSince(LocalDate.now());
    mediumMeter2.setResettable(false);

    mediumMeter3 = new MediumMeter();
    mediumMeter3.setNumber("Inactive, Resettable");
    mediumMeter3.setActiveSince(LocalDate.now().minusDays(1));
    mediumMeter3.setActiveUntil(LocalDate.now());
    mediumMeter3.setResettable(true);

    mediumMeter4 = new MediumMeter();
    mediumMeter4.setNumber("Inactive, Not resettable");
    mediumMeter4.setActiveSince(LocalDate.now().minusDays(1));
    mediumMeter4.setActiveUntil(LocalDate.now());
    mediumMeter4.setResettable(false);

    mediumMeter1.setMediumConnection(medium1);
    mediumMeter2.setMediumConnection(medium1);
    mediumMeter3.setMediumConnection(medium1);
    mediumMeter4.setMediumConnection(medium1);

    mediumMeter1 = mediumMeterRepository.save(mediumMeter1);
    mediumMeter2 = mediumMeterRepository.save(mediumMeter2);
    mediumMeter3 = mediumMeterRepository.save(mediumMeter3);
    mediumMeter4 = mediumMeterRepository.save(mediumMeter4);

    medium2 = new MediumConnection();
    medium2.setMediumName("Prąd");
    medium2 = mediumConnectionRepository.save(medium2);


  }

  @Test
  @DisplayName("result of get request to media page should "
      + "contain names of saved media, links to their pages, title of page, "
      + "and link to media connection adding page")
  public void httpGet_returnsMediaPage() {

    int numberOfMedia = mediumConnectionService.getNamesList().size();

    String destinationUrl = urlPrefix + Mappings.MEDIA_PAGE;

    ResponseEntity<String> responseEntity =
        requestSenderService.sendGet(destinationUrl);

    assertEquals(200, responseEntity.getStatusCodeValue());

    Document webPage = Jsoup.parse(responseEntity.getBody());

    // page title header
    String mediaConnectionsText = getMessage("page.mediaConnections");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(mediaConnectionsText);


    // number of media
    Element mediaList = webPage.getElementById("namesList");

    Elements media = mediaList.select("li");

    assertThat(media.size()).isEqualTo(numberOfMedia);

    // proper media links
    assertTrue(media.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(medium1.getMediumName())
            && a.attr("href")
            .equals(Mappings.MEDIUM_PAGE + "/" + medium1.getId())));

    assertTrue(media.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(medium2.getMediumName())
            && a.attr("href")
            .equals(Mappings.MEDIUM_PAGE + "/" + medium2.getId())));


    // media adding link
    String linkText = getMessage("page.addMedium");

    assertTrue(webPage.select("a").stream().anyMatch(a ->
        a.text().equals(linkText) && a.attr("href")
            .equals(Mappings.MEDIA_ADD)));
  }

  private String getMessage(String messageCode) {
    return messageSource
        .getMessage(messageCode, null,
            LocaleContextHolder.getLocale());
  }


  @Test
  @DisplayName("result of get request to existing medium page should "
      + "contain title of page, name of medium and link to medium meters page")
  public void httpGet_returnsMediumPage() {
    String destinationUrl =
        urlPrefix + Mappings.MEDIUM_PAGE + "/" + medium1.getId();

    ResponseEntity<String> responseEntity =
        requestSenderService.sendGet(destinationUrl);

    String responseBody = responseEntity.getBody();

    assertEquals(200, responseEntity.getStatusCodeValue());

    Document webPage = Jsoup.parse(responseBody);

    // page title header
    String mediaConnectionsText = getMessage("page.connectedMedium");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(mediaConnectionsText);


    // meters link
    String metersLinkText = getMessage("page.meters");
    String metersLinkHref = Mappings.MEDIUM_PAGE + "/" + medium1.getId()
        + Mappings.METERS_SUBPAGE;

    Elements pageLinks = webPage.select("a");
    assertTrue(pageLinks.stream().anyMatch(
        l -> l.text().equals(metersLinkText) && l.attr("href")
            .equals(metersLinkHref)));

    // medium name
    assertThat(
        webPage.getElementsContainingOwnText(medium1.getMediumName()).size()
            == 1);
  }

  @Test
  @DisplayName("result of get request to medium meters subpage without any "
      + "parameter should contain page title, medium name, "
      + "and list of active medium meters, showInactive button "
      + "and addMeter button")
  public void httpGet_returnsDefaultMediumMetersList() {

    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_SUBPAGE;

    ResponseEntity<String> responseEntity =
        requestSenderService.sendGet(destinationUrl);

    assertEquals(200, responseEntity.getStatusCodeValue());
    String responseBody = responseEntity.getBody();

    // page title header
    Document webPage = Jsoup.parse(responseBody);


    String metersText = getMessage("page.meters");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(metersText);


    // medium name
    assertEquals(1, webPage
        .getElementsContainingOwnText("[" + medium1.getMediumName() + "]")
        .size());

    // number of list entries
    Element mediaList = webPage.getElementById("namesList");

    Elements meters = mediaList.select("li");

    assertThat(meters.size()).isEqualTo(
        mediumConnectionService.getMeterNumbers(medium1.getId(), null, false)
            .size());

    // proper meters links
    assertTrue(meters.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(mediumMeter1.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + mediumMeter1.getId())));

    assertTrue(meters.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(mediumMeter2.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + mediumMeter2.getId())));


    // show inactive link
    String showInactiveLinkText =
        getMessage("page.showInactive");
    String showInactiveLinkHref = "?inactive=true";

    Elements pageLinks = webPage.select("a");
    assertTrue(pageLinks.stream().anyMatch(
        l -> l.text().equals(showInactiveLinkText) && l.attr("href")
            .equals(showInactiveLinkHref)));


    // meter add link
    String addMeterLinkText = getMessage("page.addMeter");
    String addMeterLinkHref = Mappings.MEDIUM_PAGE + "/" + medium1.getId()
        + Mappings.METERS_ADD_SUBPAGE;

    assertTrue(pageLinks.stream().anyMatch(
        l -> l.text().equals(addMeterLinkText) && l.attr("href")
            .equals(addMeterLinkHref)));
  }

  @Test
  @DisplayName(
      "result of get request to medium meters subpage with inactive=false "
          + "parameter should contain page title, medium name, "
          + "and list of active medium meters, showInactive button "
          + "and addMeter button")
  public void httpGet_returnsActiveMediumMetersList() {

    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_SUBPAGE + "?inactive=false";

    ResponseEntity<String> responseEntity =
        requestSenderService.sendGet(destinationUrl);

    assertEquals(200, responseEntity.getStatusCodeValue());
    String responseBody = responseEntity.getBody();

    // page title header
    Document webPage = Jsoup.parse(responseBody);


    String metersText = getMessage("page.meters");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(metersText);


    // medium name
    assertEquals(1, webPage
        .getElementsContainingOwnText("[" + medium1.getMediumName() + "]")
        .size());

    // number of list entries
    Element metersList = webPage.getElementById("namesList");

    Elements meters = metersList.select("li");

    assertThat(meters.size()).isEqualTo(
        mediumConnectionService.getMeterNumbers(medium1.getId(), null, false)
            .size());

    // proper meters links
    assertTrue(meters.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(mediumMeter1.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + mediumMeter1.getId())));

    assertTrue(meters.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(mediumMeter2.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + mediumMeter2.getId())));


    // show inactive link
    String showInactiveLinkText =
        getMessage("page.showInactive");
    String showInactiveLinkHref = "?inactive=true";

    Elements pageLinks = webPage.select("a");
    assertTrue(pageLinks.stream().anyMatch(
        l -> l.text().equals(showInactiveLinkText) && l.attr("href")
            .equals(showInactiveLinkHref)));


    // meter add link
    String addMeterLinkText = getMessage("page.addMeter");
    String addMeterLinkHref = Mappings.MEDIUM_PAGE + "/" + medium1.getId()
        + Mappings.METERS_ADD_SUBPAGE;

    assertTrue(pageLinks.stream().anyMatch(
        l -> l.text().equals(addMeterLinkText) && l.attr("href")
            .equals(addMeterLinkHref)));
  }

  @Test
  @DisplayName(
      "result of get request to medium meters subpage with inactive=true "
          + "parameter should contain page title, medium name, "
          + "and list of inactive medium meters , showActive button "
          + "and addMeter button")
  public void httpGet_returnsInactiveMediumMetersList() {
    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_SUBPAGE + "?inactive=true";

    ResponseEntity<String> responseEntity =
        requestSenderService.sendGet(destinationUrl);

    assertEquals(200, responseEntity.getStatusCodeValue());
    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);


    // page title header
    String metersText = getMessage("page.meters");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(metersText);


    // medium name
    assertEquals(1, webPage
        .getElementsContainingOwnText("[" + medium1.getMediumName() + "]")
        .size());

    // number of list entries
    Element mediaList = webPage.getElementById("namesList");

    Elements meters = mediaList.select("li");

    assertThat(meters.size()).isEqualTo(
        mediumConnectionService.getMeterNumbers(medium1.getId(), null, false)
            .size());

    // proper meters links
    assertTrue(meters.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(mediumMeter3.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + mediumMeter3.getId())));

    assertTrue(meters.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(mediumMeter4.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + mediumMeter4.getId())));


    // show active link
    String shoInactiveLinkText =
        getMessage("page.showActive");
    String showInactiveLinkHref = "?inactive=false";

    Elements pageLinks = webPage.select("a");
    assertTrue(pageLinks.stream().anyMatch(
        l -> l.text().equals(shoInactiveLinkText) && l.attr("href")
            .equals(showInactiveLinkHref)));


    // meter add link
    String addMeterLinkText = getMessage("page.addMeter");
    String addMeterLinkHref = Mappings.MEDIUM_PAGE + "/" + medium1.getId()
        + Mappings.METERS_ADD_SUBPAGE;

    assertTrue(pageLinks.stream().anyMatch(
        l -> l.text().equals(addMeterLinkText) && l.attr("href")
            .equals(addMeterLinkHref)));
  }

  @Test
  @DisplayName(
      "result of get request to medium connection adding page should contain"
          + "page title, medium name label, medium name input and add button")
  public void httpGet_returnsMediumConnectionAddingPage() {

    String destinationUrl = urlPrefix + Mappings.MEDIA_ADD;

    ResponseEntity<String> responseEntity =
        requestSenderService.sendGet(destinationUrl);

    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);

    assertEquals(200, responseEntity.getStatusCodeValue());

    // page title header
    String addNewMediumText = getMessage("page.addMedium");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(addNewMediumText);

    Element form = webPage.getElementById("newMediumForm");
    Element label = form.select("label").get(0);

    String labelText = getMessage("page.mediumName");

    assertThat(label.text()).isEqualTo(labelText);

    Element input = form.select("input").get(0);
    assertThat(input.attr("name")).isEqualTo(Attributes.MEDIUM_NAME);
    assertThat(input.attr("type")).isEqualTo("text");

    Element button = form.select("button").get(0);
    assertThat(button.attr("type")).isEqualTo("submit");

    String addButtonText = getMessage("page.addButton");

    assertThat(button.text()).isEqualTo(addButtonText);

  }

  @Test
  @DisplayName(
      "post request to medium connection adding page should add medium meter "
          + "with provided name and redirect to valid page with added medium "
          + "on the list")
  public void httpPost_addsMediumConnectionWithGivenName() {

    int initialNumberOfMedia = mediumConnectionService.getNamesList().size();

    String addedMediumName = "bbb";

    String destinationUrl =
        urlPrefix + Mappings.MEDIA_ADD + "?"
            + Attributes.MEDIUM_NAME + "=" + addedMediumName;

    ResponseEntity<String> responseEntity =
        requestSenderService.sendPost(destinationUrl);

    Long addedMediumId = mediumConnectionService.getNamesList().stream()
        .map(NumberedName::getId).max(Long::compareTo).get();

    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

    String responseBody = responseEntity.getBody();
    
    Document webPage = Jsoup.parse(responseBody);
    Element mediaList = webPage.getElementById("namesList");

    Elements media = mediaList.select("li");


    // added medium link on page
    assertThat(media.size()).isEqualTo(initialNumberOfMedia + 1);

    assertTrue(media.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(addedMediumName)
            && a.attr("href")
            .equals(Mappings.MEDIUM_PAGE + "/" + addedMediumId)));

  }

  @Test
  @DisplayName(
      "result of get request to medium meter adding page should contain "
          + "page title, and meter adding form with proper fields and button")
  public void httpGet_showsMetersAddingForm() {

    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    ResponseEntity<String> responseEntity =
        requestSenderService.sendGet(destinationUrl);

    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);

    Element form = webPage.getElementById(Attributes.MEDIUM_METER_FORM);

    assertEquals(form.attr("action"), Mappings.MEDIUM_PAGE
        + "/" + medium1.getId() + Mappings.METERS_ADD_SUBPAGE);
    assertEquals(form.attr("method"), "post");

    Elements labels = form.select("div > label");


    assertEquals(6, labels.size());
    String meterNumberText = getMessage("page.meterNumber");
    assertTrue(labels.stream().anyMatch(l -> l.text().equals(meterNumberText)));

    String meterUnitText = getMessage("page.meterUnit");
    assertTrue(labels.stream().anyMatch(l -> l.text().equals(meterUnitText)));

    String resettableText = getMessage("page.resettable");
    assertTrue(labels.stream().anyMatch(l -> l.text().equals(resettableText)));

    String activeSinceText = getMessage("page.activeSince");
    assertTrue(labels.stream().anyMatch(l -> l.text().equals(activeSinceText)));

    String firstReadingText = getMessage("page.firstReading");
    assertTrue(
        labels.stream().anyMatch(l -> l.text().equals(firstReadingText)));

    Elements inputs = form.select("div > input");

    assertEquals(7, inputs.size());

    assertTrue(inputs.stream()
        .anyMatch(i -> i.attr("name").equals(Attributes.METER_NUMBER)
            && i.attr("type").equals("text")));

    assertTrue(inputs.stream()
        .anyMatch(i -> i.attr("name").equals(Attributes.METER_UNIT)
            && i.attr("type").equals("text")));

    assertTrue(inputs.stream()
        .anyMatch(i -> i.attr("name").equals(Attributes.ACTIVE_SINCE)
            && i.attr("type").equals("date")));

    assertTrue(inputs.stream()
        .anyMatch(i -> i.attr("name").equals(Attributes.FIRST_READING)
            && i.attr("type").equals("number")));

    assertTrue(inputs.stream()
        .anyMatch(i -> i.attr("name").equals("resettable")
            && i.attr("type").equals("checkbox")));


    assertTrue(inputs.stream()
        .anyMatch(i -> i.attr("name").equals("resettable")
            && i.attr("type").equals("checkbox")));

    assertTrue(inputs.stream()
        .anyMatch(i -> i.attr("name").equals("_resettable")
            && i.attr("type").equals("hidden")));

    assertTrue(inputs.stream()
        .anyMatch(i -> i.attr("name").equals("hasFirstReading")
            && i.attr("type").equals("checkbox")));

    Element button = form.select("button").get(0);
    assertTrue(button.attr("type").equals("submit") && button.text()
        .equals(getMessage("page.addButton")));
  }


  @Test
  @DisplayName(
      "result of post request to medium meter adding page with good meter form "
          + "should add new medium meter")
  public void httpPost_addsMediumMeter() {
    int initialNumberOfMeters =
        mediumConnectionRepository
            .fetchActiveMeterNumbers(medium1.getId(), null)
            .getSize() + mediumConnectionRepository
            .fetchInactiveMeterNumbers(medium1.getId(), null)
            .getSize();

    // request data
    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;


    String meterNumber = "123";
    String meterUnit = "kwh";
    String activeSince = LocalDate.now().toString();
    String resettable = "true";
    String firstReading = "12";


    MultiValueMap<String, String> formInputs = new LinkedMultiValueMap<>();
    formInputs.add(Attributes.METER_NUMBER, meterNumber);
    formInputs.add(Attributes.METER_UNIT, meterUnit);
    formInputs.add(Attributes.ACTIVE_SINCE, activeSince);
    formInputs.add("resettable", resettable);
    formInputs.add("firstReading", firstReading);

    // sending request
    ResponseEntity<String> responseEntity =
        requestSenderService.sendPost(destinationUrl, formInputs);

    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    String responseBody = responseEntity.getBody();

    // number of meters increased after post
    long resultNumberOfMeters =
        mediumConnectionRepository
            .fetchActiveMeterNumbers(medium1.getId(), null)
            .getSize() + mediumConnectionRepository
            .fetchInactiveMeterNumbers(medium1.getId(), null)
            .getSize();

    assertEquals(resultNumberOfMeters, initialNumberOfMeters + 1);

    // proper medium meter added to db
    Long addedMediumMeterId =
        mediumConnectionRepository.fetchMeterNumbers(medium1.getId()).stream()
            .map(mn -> mn.getId()).max(Long::compareTo).get();

    MediumMeter addedMediumMeter =
        mediumMeterRepository.findById(addedMediumMeterId).get();

    List<ReadingBasics> readings =
        readingRepository.findByMediumMeterId(addedMediumMeter.getId());

    assertEquals(1, readings.size());
    assertEquals(Double.parseDouble(firstReading),
        readings.get(0).getReading());
    assertEquals(LocalDate.parse(activeSince), readings.get(0).getDate());


    assertEquals(addedMediumMeter.getNumber(), meterNumber);
    assertEquals(addedMediumMeter.getUnitName(), meterUnit);
    assertEquals(addedMediumMeter.getActiveSince(),
        LocalDate.parse(activeSince));
    assertEquals(addedMediumMeter.isResettable(), Boolean.valueOf(resettable));


    // link to added meter on result page
    Document webPage = Jsoup.parse(responseBody);
    Element metersList = webPage.getElementById("namesList");

    Elements meters = metersList.select("li");

    assertTrue(meters.stream().map(m -> m.select("a").get(0))
        .anyMatch(a -> a.text().equals(addedMediumMeter.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + addedMediumMeter.getId())));
  }


  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (inputs are null) should show same page with 3 errors")
  public void httpPost_invalidFormNullInputs() {
    // request data
    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;


    MultiValueMap<String, String> formInputs =
        new LinkedMultiValueMap<>();

    formInputs.add(Attributes.FIRST_READING, null);
    formInputs.add(Attributes.ACTIVE_SINCE, null);

    // sending request
    ResponseEntity<String> responseEntity =
        requestSenderService.sendPost(destinationUrl, formInputs);


    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);


    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);

    Element form = webPage.getElementById(Attributes.MEDIUM_METER_FORM);
    assertNotNull(form);

    Elements errorsParagraphs = form.getElementsByTag("p");

    assertEquals(4, errorsParagraphs.size());

    assertEquals(errorsMessageSource.getMessage("error.empty"),
        errorsParagraphs.get(0).text());


    assertEquals(errorsMessageSource.getMessage("error.empty"),
        errorsParagraphs.get(1).text());


    assertEquals(errorsMessageSource.getMessage("error.date"),
        errorsParagraphs.get(2).text());

    assertTrue(errorsParagraphs.get(3).text()
        .contains(errorsMessageSource.getMessage("error.positiveOnly")));

    assertTrue(errorsParagraphs.get(3).text()
        .contains(errorsMessageSource.getMessage("error.number")));

    assertEquals(webPage.getElementById("header").text(),
        getMessage("page.addMeter"));

  }


  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (inputs are blank) should show same page with 3 errors")
  public void httpPost_invalidFormBlankInputs() {
    // request data
    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> formInputs =
        new LinkedMultiValueMap<>();

    String meterNumber = " ";
    String meterUnit = " ";
    String activeSince = " ";
    String firstReading = " ";

    formInputs.add("firstReading", firstReading);
    formInputs.add(Attributes.METER_NUMBER, meterNumber);
    formInputs.add(Attributes.METER_UNIT, meterUnit);
    formInputs.add(Attributes.ACTIVE_SINCE, activeSince);


    formInputs.add(Attributes.ACTIVE_SINCE, activeSince);


    HttpEntity<MultiValueMap<String, String>>
        request = new HttpEntity<>(formInputs, headers);

    // sending request
    ResponseEntity<String> responseEntity =
        requestSenderService.sendPost(destinationUrl, formInputs);

    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);

    Element form = webPage.getElementById(Attributes.MEDIUM_METER_FORM);
    assertNotNull(form);

    Elements errorsParagraphs = form.getElementsByTag("p");

    assertEquals(4, errorsParagraphs.size());

    assertEquals(errorsMessageSource.getMessage("error.empty"),
        errorsParagraphs.get(0).text());


    assertEquals(errorsMessageSource.getMessage("error.empty"),
        errorsParagraphs.get(1).text());


    assertEquals(errorsMessageSource.getMessage("error.date"),
        errorsParagraphs.get(2).text());

    assertTrue(errorsParagraphs.get(3).text()
        .contains(errorsMessageSource.getMessage("error.positiveOnly")));

    assertTrue(errorsParagraphs.get(3).text()
        .contains(errorsMessageSource.getMessage("error.number")));

    assertEquals(webPage.getElementById("header").text(),
        getMessage("page.addMeter"));

  }


  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (inputs are empty) should show same page with 3 errors")
  public void httpPost_invalidFormEmptyInputs() {
    // request data
    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    MultiValueMap<String, String> formInputs =
        new LinkedMultiValueMap<>();

    String meterNumber = "";
    String meterUnit = "";
    String activeSince = "";
    String firstReading = "";

    formInputs.add("firstReading", firstReading);
    formInputs.add(Attributes.METER_NUMBER, meterNumber);
    formInputs.add(Attributes.METER_UNIT, meterUnit);
    formInputs.add(Attributes.ACTIVE_SINCE, activeSince);


    formInputs.add(Attributes.ACTIVE_SINCE, activeSince);

    // sending request
    ResponseEntity<String> responseEntity =
        requestSenderService.sendPost(destinationUrl, formInputs);

    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);

    Element form = webPage.getElementById(Attributes.MEDIUM_METER_FORM);
    assertNotNull(form);

    Elements errorsParagraphs = form.getElementsByTag("p");

    assertEquals(4, errorsParagraphs.size());

    assertEquals(errorsMessageSource.getMessage("error.empty"),
        errorsParagraphs.get(0).text());


    assertEquals(errorsMessageSource.getMessage("error.empty"),
        errorsParagraphs.get(1).text());


    assertEquals(errorsMessageSource.getMessage("error.date"),
        errorsParagraphs.get(2).text());

    assertTrue(errorsParagraphs.get(3).text()
        .contains(errorsMessageSource.getMessage("error.positiveOnly")));

    assertTrue(errorsParagraphs.get(3).text()
        .contains(errorsMessageSource.getMessage("error.number")));

    assertEquals(webPage.getElementById("header").text(),
        getMessage("page.addMeter"));

  }


  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (date is in wrong format) should show same page"
          + " with 1 error")
  public void httpPost_invalidFormWrongDate() {
    // request data
    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    MultiValueMap<String, String> formInputs =
        new LinkedMultiValueMap<>();

    String meterNumber = "number";
    String meterUnit = "unit";
    String activeSince = "wrong date format!";
    String firstReading = "15.5";

    formInputs.add(Attributes.FIRST_READING, firstReading);
    formInputs.add(Attributes.METER_NUMBER, meterNumber);
    formInputs.add(Attributes.METER_UNIT, meterUnit);
    formInputs.add(Attributes.ACTIVE_SINCE, activeSince);


    formInputs.add(Attributes.ACTIVE_SINCE, activeSince);


    // sending request
    ResponseEntity<String> responseEntity =
        requestSenderService.sendPost(destinationUrl, formInputs);


    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);

    Element form = webPage.getElementById(Attributes.MEDIUM_METER_FORM);
    assertNotNull(form);

    Elements errorsParagraphs = form.getElementsByTag("p");

    assertEquals(1, errorsParagraphs.size());

    assertEquals(errorsMessageSource.getMessage("error.date"),
        errorsParagraphs.get(0).text());

    assertEquals(webPage.getElementById("header").text(),
        getMessage("page.addMeter"));
  }

  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (reading value is in wrong format) should show same "
          + "page with 1 error")
  public void httpPost_invalidFormWrongReadingFormat() {
    // request data
    String destinationUrl = urlPrefix + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    MultiValueMap<String, String> formInputs =
        new LinkedMultiValueMap<>();

    String meterNumber = "number";
    String meterUnit = "unit";
    String firstReading = "wrong reading format";

    formInputs.add("firstReading", firstReading);
    formInputs.add(Attributes.METER_NUMBER, meterNumber);
    formInputs.add(Attributes.METER_UNIT, meterUnit);

    // sending request
    ResponseEntity<String> responseEntity =
        requestSenderService.sendPost(destinationUrl, formInputs);


    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);

    Element form = webPage.getElementById(Attributes.MEDIUM_METER_FORM);
    assertNotNull(form);

    Elements errorsParagraphs = form.getElementsByTag("p");

    assertEquals(1, errorsParagraphs.size());

    assertTrue(errorsParagraphs.get(0).text()
        .contains(errorsMessageSource.getMessage("error.positiveOnly")));

    assertTrue(errorsParagraphs.get(0).text()
        .contains(errorsMessageSource.getMessage("error.number")));

    assertEquals(webPage.getElementById("header").text(),
        getMessage("page.addMeter"));

  }
}