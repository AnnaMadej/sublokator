package com.aniamadej.sublokator.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import com.aniamadej.sublokator.CustomMessageSource;
import com.aniamadej.sublokator.model.MediumConnection;
import com.aniamadej.sublokator.model.MediumMeter;
import com.aniamadej.sublokator.repository.MediumConnectionRepository;
import com.aniamadej.sublokator.repository.MediumMeterRepository;
import com.aniamadej.sublokator.service.MediumConnectionService;
import com.aniamadej.sublokator.util.Attributes;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MediaControllerE2ETests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate testRestTemplate;

  @Autowired
  private MediumConnectionService mediumConnectionService;

  @Autowired
  private
  MediumConnectionRepository mediumConnectionRepository;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private CustomMessageSource errorsMessageSource;

  @Autowired
  MediumMeterRepository mediumMeterRepository;

  private MediumConnection medium1;
  private MediumConnection medium2;

  private MediumMeter mediumMeter1;
  private MediumMeter mediumMeter2;
  private MediumMeter mediumMeter3;
  private MediumMeter mediumMeter4;

  @BeforeAll
  public void init() {

    medium1 = new MediumConnection();
    medium1.setMediumName("Gaz");
    medium2 = new MediumConnection();
    medium2.setMediumName("Prąd");
    medium1 = mediumConnectionRepository.save(medium1);
    medium2 = mediumConnectionRepository.save(medium2);

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

  }

  @Test
  @DisplayName("result of get request to media page should "
      + "contain names of saved media, links to their pages, title of page, "
      + "and link to media connection adding page")
  public void httpGet_returnsMediaPage() {

    int numberOfMedia = mediumConnectionService.getNamesList().size();

    ResponseEntity<String> responseEntity =
        testRestTemplate
            .getForEntity("http://localhost:" + port + Mappings.MEDIA_PAGE,
                String.class);
    String responseBody = responseEntity.getBody();

    assertEquals(200, responseEntity.getStatusCodeValue());

    Document webPage = Jsoup.parse(responseBody);

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
    assertTrue(media.stream().map(m -> m.select("a").first())
        .anyMatch(a -> a.text().equals(medium1.getMediumName())
            && a.attr("href")
            .equals(Mappings.MEDIUM_PAGE + "/" + medium1.getId())));

    assertTrue(media.stream().map(m -> m.select("a").first())
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
  public void httpGet_returnsMediumPage() throws Exception {
    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity(
            "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/" + medium1
                .getId(),
            String.class);

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
    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity(
            "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
                + medium1.getId() + Mappings.METERS_SUBPAGE,
            String.class);

    assertEquals(200, responseEntity.getStatusCodeValue());
    String responseBody = responseEntity.getBody();

    // page title header
    String mediaConnectionsText = getMessage("page.connectedMedium");
    Document webPage = Jsoup.parse(responseBody);


    String metersText = getMessage("page.meters");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(metersText);


    // medium name
    assertTrue(
        webPage
            .getElementsContainingOwnText("[" + medium1.getMediumName() + "]")
            .size() == 1);

    // number of list entries
    Element mediaList = webPage.getElementById("namesList");

    Elements meters = mediaList.select("li");

    assertThat(meters.size()).isEqualTo(
        mediumConnectionService.getMeterNumbers(medium1.getId(), null, false)
            .size());

    // proper meters links
    assertTrue(meters.stream().map(m -> m.select("a").first())
        .anyMatch(a -> a.text().equals(mediumMeter1.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + mediumMeter1.getId())));

    assertTrue(meters.stream().map(m -> m.select("a").first())
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
    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity(
            "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
                + medium1.getId() + Mappings.METERS_SUBPAGE + "?inactive=false",
            String.class);

    assertEquals(200, responseEntity.getStatusCodeValue());
    String responseBody = responseEntity.getBody();

    // page title header
    String mediaConnectionsText = getMessage("page.connectedMedium");
    Document webPage = Jsoup.parse(responseBody);


    String metersText = getMessage("page.meters");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(metersText);


    // medium name
    assertTrue(
        webPage
            .getElementsContainingOwnText("[" + medium1.getMediumName() + "]")
            .size() == 1);

    // number of list entries
    Element metersList = webPage.getElementById("namesList");

    Elements meters = metersList.select("li");

    assertThat(meters.size()).isEqualTo(
        mediumConnectionService.getMeterNumbers(medium1.getId(), null, false)
            .size());

    // proper meters links
    assertTrue(meters.stream().map(m -> m.select("a").first())
        .anyMatch(a -> a.text().equals(mediumMeter1.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + mediumMeter1.getId())));

    assertTrue(meters.stream().map(m -> m.select("a").first())
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
    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity(
            "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
                + medium1.getId() + Mappings.METERS_SUBPAGE + "?inactive=true",
            String.class);

    assertEquals(200, responseEntity.getStatusCodeValue());
    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);


    // page title header
    String mediaConnectionsText = getMessage("page.connectedMedium");

    String metersText = getMessage("page.meters");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(metersText);


    // medium name
    assertTrue(
        webPage
            .getElementsContainingOwnText("[" + medium1.getMediumName() + "]")
            .size() == 1);

    // number of list entries
    Element mediaList = webPage.getElementById("namesList");

    Elements meters = mediaList.select("li");

    assertThat(meters.size()).isEqualTo(
        mediumConnectionService.getMeterNumbers(medium1.getId(), null, false)
            .size());

    // proper meters links
    assertTrue(meters.stream().map(m -> m.select("a").first())
        .anyMatch(a -> a.text().equals(mediumMeter3.getNumber())
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + mediumMeter3.getId())));

    assertTrue(meters.stream().map(m -> m.select("a").first())
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

    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity("http://localhost:" + port + Mappings.MEDIA_ADD,
            String.class);
    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);

    assertEquals(200, responseEntity.getStatusCodeValue());

    // page title header
    String addMediumText = getMessage("page.addMedium");

    String addNewMediumText = getMessage("page.addMedium");

    assertThat(webPage.getElementById("pageHeader")).isNotNull();
    assertThat(webPage.getElementById("pageHeader").text())
        .contains(addNewMediumText);

    Element form = webPage.getElementById("newMediumForm");
    Element label = form.select("label").first();

    String labelText = getMessage("page.mediumName");

    assertThat(label.text()).isEqualTo(labelText);

    Element input = form.select("input").first();
    assertThat(input.attr("name")).isEqualTo(Attributes.MEDIUM_NAME);
    assertThat(input.attr("type")).isEqualTo("text");

    Element button = form.select("button").first();
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

    String addedMediumName = "nowe medium";

    // no new medium in database yet
    assertTrue(mediumConnectionService.getNamesList().stream()
        .noneMatch(name -> name.getName().equals(addedMediumName)));

    String url =
        "http://localhost:" + port + Mappings.MEDIA_ADD + "?"
            + Attributes.MEDIUM_NAME + "=" + addedMediumName;

    ResponseEntity<String> responseEntity =
        testRestTemplate.postForEntity(url, null,
            String.class);
    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);


    // medium added to database
    assertTrue(mediumConnectionService.getNamesList().stream()
        .anyMatch(name -> name.getName().equals(addedMediumName)
            && name.getId() == initialNumberOfMedia + 1));


    String responseBody = responseEntity.getBody();
    Document webPage = Jsoup.parse(responseBody);
    Element mediaList = webPage.getElementById("namesList");

    Elements media = mediaList.select("li");


    // added medium link on page
    assertThat(media.size()).isEqualTo(initialNumberOfMedia + 1);

    assertTrue(media.stream().map(m -> m.select("a").first())
        .anyMatch(a -> a.text().equals(addedMediumName)
            && a.attr("href")
            .equals(Mappings.MEDIUM_PAGE + "/" + (initialNumberOfMedia + 1))));

  }

  @Test
  @DisplayName(
      "result of get request to medium meter adding page should contain "
          + "page title, and meter adding form with proper fields and button")
  public void httpGet_showsMetersAddingForm() {

    String url = "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;
    ResponseEntity<String> responseEntity = testRestTemplate
        .getForEntity(
            url,
            String.class);
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

    Element button = form.select("button").first();
    assertTrue(button.attr("type").equals("submit") && button.text()
        .equals(getMessage("page.addButton")));
  }

  @Transactional
  @Test
  @DisplayName(
      "result of post request to medium meter adding page with good meter form "
          + "should add new medium meter")
  public void httpPost_addsMetiumMeter() {
    int initialNumberOfMeters =
        mediumConnectionRepository
            .fetchActiveMeterNumbers(medium1.getId(), null)
            .getSize() + mediumConnectionRepository
            .fetchInactiveMeterNumbers(medium1.getId(), null)
            .getSize();

    // request data
    String url = "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map =
        new LinkedMultiValueMap<String, String>();

    String meterNumber = "123";
    String meterUnit = "kwh";
    String activeSince = LocalDate.now().toString();
    String resettable = "true";
    String firstReading = "12";

    map.add(Attributes.METER_NUMBER, meterNumber);
    map.add(Attributes.METER_UNIT, meterUnit);
    map.add(Attributes.ACTIVE_SINCE, activeSince);
    map.add("resettable", resettable);
    map.add("firstReading", firstReading);


    HttpEntity<MultiValueMap<String, String>>
        request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

    // sending request
    ResponseEntity<String> responseEntity =
        testRestTemplate.postForEntity(url, request,
            String.class);

    assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    String responseBody = responseEntity.getBody();

    // number of meters increased after post
    int resultNumberOfMeters =
        mediumConnectionRepository
            .fetchActiveMeterNumbers(medium1.getId(), null)
            .getSize() + mediumConnectionRepository
            .fetchInactiveMeterNumbers(medium1.getId(), null)
            .getSize();

    assertEquals(resultNumberOfMeters, initialNumberOfMeters + 1);

    // correct medium meter added
    MediumMeter addedMediumMeter =
        mediumMeterRepository.findById((long) resultNumberOfMeters).get();

    assertEquals(addedMediumMeter.getNumber(), meterNumber);
    assertEquals(addedMediumMeter.getUnitName(), meterUnit);
    assertEquals(addedMediumMeter.getActiveSince(),
        LocalDate.parse(activeSince));
    assertEquals(addedMediumMeter.getReadings().size(), 1);
    assertEquals(addedMediumMeter.getReadings().get(0).getReading(),
        Double.parseDouble(firstReading));
    assertEquals(addedMediumMeter.getReadings().get(0).getDate(),
        LocalDate.parse(activeSince));
    assertEquals(addedMediumMeter.isResettable(), Boolean.valueOf(resettable));


    // link to added meter on result page
    Document webPage = Jsoup.parse(responseBody);
    Element metersList = webPage.getElementById("namesList");

    Elements meters = metersList.select("li");
    assertTrue(meters.stream().map(m -> m.select("a").first())
        .anyMatch(a -> a.text().equals(meterNumber)
            && a.attr("href")
            .equals(Mappings.METER_PAGE + "/" + resultNumberOfMeters)));
  }

  @Transactional
  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (inputs are null) should show same page with 3 errors")
  public void httpPost_invalidFormNullInputs() {
    // request data
    String url = "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map =
        new LinkedMultiValueMap<String, String>();

    String activeSince = null;
    String firstReading = null;

    map.add("firstReading", firstReading);
    map.add(Attributes.ACTIVE_SINCE, activeSince);


    HttpEntity<MultiValueMap<String, String>>
        request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

    // sending request
    ResponseEntity<String> responseEntity =
        testRestTemplate.postForEntity(url, request,
            String.class);


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

  @Transactional
  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (inputs are blank) should show same page with 3 errors")
  public void httpPost_invalidFormBlankInputs() {
    // request data
    String url = "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map =
        new LinkedMultiValueMap<String, String>();

    String meterNumber = " ";
    String meterUnit = " ";
    String activeSince = " ";
    String firstReading = " ";

    map.add("firstReading", firstReading);
    map.add(Attributes.METER_NUMBER, meterNumber);
    map.add(Attributes.METER_UNIT, meterUnit);
    map.add(Attributes.ACTIVE_SINCE, activeSince);


    map.add(Attributes.ACTIVE_SINCE, activeSince);


    HttpEntity<MultiValueMap<String, String>>
        request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

    // sending request
    ResponseEntity<String> responseEntity =
        testRestTemplate.postForEntity(url, request,
            String.class);


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

  @Transactional
  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (inputs are empty) should show same page with 3 errors")
  public void httpPost_invalidFormEmptyInputs() {
    // request data
    String url = "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map =
        new LinkedMultiValueMap<String, String>();

    String meterNumber = "";
    String meterUnit = "";
    String activeSince = "";
    String firstReading = "";

    map.add("firstReading", firstReading);
    map.add(Attributes.METER_NUMBER, meterNumber);
    map.add(Attributes.METER_UNIT, meterUnit);
    map.add(Attributes.ACTIVE_SINCE, activeSince);


    map.add(Attributes.ACTIVE_SINCE, activeSince);


    HttpEntity<MultiValueMap<String, String>>
        request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

    // sending request
    ResponseEntity<String> responseEntity =
        testRestTemplate.postForEntity(url, request,
            String.class);


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

  @Transactional
  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (date is in wrong format) should show same page"
          + " with 1 error")
  public void httpPost_invalidFormWrongDate() {
    // request data
    String url = "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map =
        new LinkedMultiValueMap<String, String>();

    String meterNumber = "number";
    String meterUnit = "unit";
    String activeSince = "wrong date format!";
    String firstReading = "15.5";

    map.add("firstReading", firstReading);
    map.add(Attributes.METER_NUMBER, meterNumber);
    map.add(Attributes.METER_UNIT, meterUnit);
    map.add(Attributes.ACTIVE_SINCE, activeSince);


    map.add(Attributes.ACTIVE_SINCE, activeSince);


    HttpEntity<MultiValueMap<String, String>>
        request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

    // sending request
    ResponseEntity<String> responseEntity =
        testRestTemplate.postForEntity(url, request,
            String.class);


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

  @Transactional
  @Test
  @DisplayName(
      "result of post request to medium meter adding page with invalid "
          + "meter form (reading value is in wrong format) should show same "
          + "page with 1 error")
  public void httpPost_invalidFormWrongReadingFormat() {
    // request data
    String url = "http://localhost:" + port + Mappings.MEDIUM_PAGE + "/"
        + medium1.getId() + Mappings.METERS_ADD_SUBPAGE;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    MultiValueMap<String, String> map =
        new LinkedMultiValueMap<String, String>();

    String meterNumber = "number";
    String meterUnit = "unit";
    String firstReading = "wrong reading format";

    map.add("firstReading", firstReading);
    map.add(Attributes.METER_NUMBER, meterNumber);
    map.add(Attributes.METER_UNIT, meterUnit);


    HttpEntity<MultiValueMap<String, String>>
        request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

    // sending request
    ResponseEntity<String> responseEntity =
        testRestTemplate.postForEntity(url, request,
            String.class);


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